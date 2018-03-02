package de.dresden.tu;

import com.univocity.parsers.fixed.FixedWidthFields;
import com.univocity.parsers.fixed.FixedWidthParser;
import com.univocity.parsers.fixed.FixedWidthParserSettings;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import de.dresden.tu.swmm.Conduit;
import de.dresden.tu.swmm.Junction;
import de.dresden.tu.swmm.Outfall;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SWMM2SHP {

    private final static String COORDINATES = "COORDINATES";
    private final static String XSECTIONS = "XSECTIONS";
    private final static String CONDUITS = "CONDUITS";
    private final static String OUTFALLS = "OUTFALLS";
    private final static String JUNCTIONS = "JUNCTIONS";

    private final static String START = "_START";
    private final static String END = "_END";

    private Map<String, Integer> marks = new HashMap<String, Integer>();
    private Map<String, int[]> lengths = new HashMap<String, int[]>();

    private Map<String, Coordinate> coordinates = new HashMap<String, Coordinate>();
    private Map<String, Conduit> conduits = new HashMap<String, Conduit>();
    private Map<String, Outfall> outfalls = new HashMap<String, Outfall>();
    private Map<String, Junction> junctions = new HashMap<String, Junction>();

    private ArrayList<String> inpFileArray = new ArrayList<String>();

    private GeometryFactory geometryFactory;
    private String folderlocation;

    private CoordinateReferenceSystem crs = null;
    private final SimpleFeatureType JUNCTION_POINT_TYPE = DataUtilities.createType("Junction", "the_geom:Point,name:String,elevation:Double,maxDepth:Double,initDepth:Double,surDepth:Double,aponded:Boolean");
    private final SimpleFeatureType OUTFALL_POINT_TYPE = DataUtilities.createType("Outfall", "the_geom:Point,name:String,elevation:Double,type:String,stageData:String,gated:Boolean,routeTo:String");
    private final SimpleFeatureType CONDUIT_POLYLINE_TYPE = DataUtilities.createType("Conduit", "the_geom:LineString,name:String,fromNode:String,toNode:String,length:Double,roughness:Double,inOffset:Double,outOffset:Double,initFlow:Double,maxFlow:Double,shape:String,geom1:Double,geom2:Double,geom3:Double,geom4:Double");

    public static void main(String[] args) throws Exception {
        SWMM2SHP swmm2shp = new SWMM2SHP();
        swmm2shp.run();
    }

    public SWMM2SHP() throws Exception {}

    public void run() throws Exception {
        //standard file
        File file = new File(this.getClass().getClassLoader().getResource("eschdorf_v6_20141208.inp").getFile());
        //choose file yourself
        file = getINPFile(file);
        //output folder
        folderlocation = getNewShapeFolder(file);
        crs = CRS.decode("EPSG:31469");
        run(file, folderlocation, crs);
    }

    public boolean run(File inpFile, String outputFolder, CoordinateReferenceSystem crs) throws Exception {

        this.folderlocation = outputFolder;
        this.crs = crs;

        BufferedReader reader = new BufferedReader(new FileReader(inpFile));

        try {
            JTSFactoryFinder.getGeometryFactory(null);
            String line;
            String current = null;
            int i = 1;
            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(line);
                inpFileArray.add(line);
                if (line.startsWith("[" + COORDINATES + "]")) {
                    marks.put(COORDINATES + START, i + 2);
                    current = COORDINATES;
                } else if (line.startsWith("[" + XSECTIONS + "]")) {
                    marks.put(XSECTIONS + START, i + 2);
                    current = XSECTIONS;
                } else if (line.startsWith("[" + CONDUITS + "]")) {
                    marks.put(CONDUITS + START, i + 2);
                    current = CONDUITS;
                } else if (line.startsWith("[" + OUTFALLS + "]")) {
                    marks.put(OUTFALLS + START, i + 2);
                    current = OUTFALLS;
                } else if (line.startsWith("[" + JUNCTIONS + "]")) {
                    marks.put(JUNCTIONS + START, i + 2);
                    current = JUNCTIONS;
                } else if (line.trim().length() == 0 && current != null) {
                    marks.put(current + END, i - 2);
                    current = null;
                } else if (line.startsWith(";;-") && current != null) {
                    // datatables do not have a column separator
                    // therefore length of every column is indicates by trailing ----- ---- -- ------
                    // identify this structure an use it for later line splitting
                    String[] thisline = splitTrimmedLine(line);
                    ArrayList<Integer> lengthsList = new ArrayList<Integer>();
                    for (String s : thisline) {
                        lengthsList.add(s.length() + 1);
                    }
                    int[] lengthArray = new int[lengthsList.size()];
                    for (int k = 0; k < lengthArray.length; k++) {
                        lengthArray[k] = lengthsList.get(k);
                    }
                    lengths.put(current, lengthArray);
                }
                i++;
            }

            readCoordinates();
            readJunctions();
            readOutfalls();
            readConduits();

            geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

            createJunctionsFeature();
            createOufallsFeature();
            createConduitFeature();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            reader.close();
        }
        return true;
    }

    private void createConduitFeature() throws IOException {
//        private final SimpleFeatureType CONDUIT_POLYLINE_TYPE = DataUtilities.createType("Conduit", "the_geom:Polyline,name:String,fromNode:String,toNode:String,length:Double,roughness:Double,inOffset:Double,outOffset:Double,initFlow:Double,maxFlow:Double,shape:String,geom1:Double,geom2:Double,geom3:Double,geom4:Double");
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(CONDUIT_POLYLINE_TYPE);
        Iterator conduitIterator = conduits.entrySet().iterator();
        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        while (conduitIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) conduitIterator.next();
            Coordinate[] linestringCoordinates = {coordinates.get(((Conduit) pair.getValue()).getFromNode()), coordinates.get(((Conduit) pair.getValue()).getToNode())};
            LineString ls = geometryFactory.createLineString(linestringCoordinates);
            featureBuilder.add(ls);
            featureBuilder.add(pair.getKey());
            featureBuilder.add(((Conduit) pair.getValue()).getFromNode());
            featureBuilder.add(((Conduit) pair.getValue()).getToNode());
            featureBuilder.add(((Conduit) pair.getValue()).getLength());
            featureBuilder.add(((Conduit) pair.getValue()).getRoughness());
            featureBuilder.add(((Conduit) pair.getValue()).getInOffset());
            featureBuilder.add(((Conduit) pair.getValue()).getOutOffset());
            featureBuilder.add(((Conduit) pair.getValue()).getInitFlow());
            featureBuilder.add(((Conduit) pair.getValue()).getMaxFlow());
            featureBuilder.add(((Conduit) pair.getValue()).getShape());
            featureBuilder.add(((Conduit) pair.getValue()).getGeom1());
            featureBuilder.add(((Conduit) pair.getValue()).getGeom2());
            featureBuilder.add(((Conduit) pair.getValue()).getGeom3());
            featureBuilder.add(((Conduit) pair.getValue()).getGeom4());
            SimpleFeature feature = featureBuilder.buildFeature(null);
            collection.add(feature);
        }

        File newFile = new File(folderlocation + "\\conduits.shp");
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        writeToShape(collection, dataStoreFactory, params, CONDUIT_POLYLINE_TYPE);
    }

    private void createJunctionsFeature() throws IOException {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(JUNCTION_POINT_TYPE);
        Iterator junctionsIterator = junctions.entrySet().iterator();
        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        while (junctionsIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) junctionsIterator.next();
            Coordinate coord = coordinates.get(pair.getKey());
            Point p = geometryFactory.createPoint(coord);
            featureBuilder.add(p);
            featureBuilder.add(pair.getKey());
            featureBuilder.add(((Junction) pair.getValue()).getElevation());
            featureBuilder.add(((Junction) pair.getValue()).getMaxDepth());
            featureBuilder.add(((Junction) pair.getValue()).getInitDepth());
            featureBuilder.add(((Junction) pair.getValue()).getSurDepth());
            featureBuilder.add(((Junction) pair.getValue()).isAponded());
            SimpleFeature feature = featureBuilder.buildFeature(null);
            collection.add(feature);
        }

        File newFile = new File(folderlocation + "\\junctions.shp");
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        writeToShape(collection, dataStoreFactory, params, JUNCTION_POINT_TYPE);
    }

    private void createOufallsFeature() throws IOException {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(OUTFALL_POINT_TYPE);
        Iterator outfallsIterator = outfalls.entrySet().iterator();
        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        while (outfallsIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) outfallsIterator.next();
            Coordinate coord = coordinates.get(pair.getKey());
            Point p = geometryFactory.createPoint(coord);
            featureBuilder.add(p);
            featureBuilder.add(pair.getKey());
            featureBuilder.add(((Outfall) pair.getValue()).getElevation());
            featureBuilder.add(((Outfall) pair.getValue()).getType());
            featureBuilder.add(((Outfall) pair.getValue()).getStageData());
            featureBuilder.add(((Outfall) pair.getValue()).isGated());
            featureBuilder.add(((Outfall) pair.getValue()).getRouteTo());
            SimpleFeature feature = featureBuilder.buildFeature(null);
            collection.add(feature);
        }

        File newFile = new File(folderlocation + "\\outfalls.shp");
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        writeToShape(collection, dataStoreFactory, params, OUTFALL_POINT_TYPE);
    }

    private void writeToShape(DefaultFeatureCollection collection, ShapefileDataStoreFactory dataStoreFactory, Map<String, Serializable> params, SimpleFeatureType sft) throws IOException {
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(sft);
        newDataStore.forceSchemaCRS(crs);
        Transaction transaction = new DefaultTransaction("create");
        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
        } else {
            System.out.println(typeName + " does not support read/write access");
        }
    }

    private void readCoordinates() {
        FixedWidthFields fwf = new FixedWidthFields(lengths.get(COORDINATES));
        FixedWidthParserSettings settings = new FixedWidthParserSettings(fwf);
        settings.setRecordEndsOnNewline(true);
        settings.setSkipTrailingCharsUntilNewline(true);
        FixedWidthParser parser = new FixedWidthParser(settings);

        for (int i = marks.get(COORDINATES + START); i <= marks.get(COORDINATES + END); i++) {
            String[] thisline = parser.parseLine(inpFileArray.get(i));
            double latitude = Double.parseDouble(thisline[2]);
            double longitude = Double.parseDouble(thisline[1]);
            coordinates.put(thisline[0], new Coordinate(longitude, latitude));
        }
    }

    private void readJunctions() {

        FixedWidthFields fwf = new FixedWidthFields(lengths.get(JUNCTIONS));
        FixedWidthParserSettings settings = new FixedWidthParserSettings(fwf);
        settings.setRecordEndsOnNewline(true);
        settings.setSkipTrailingCharsUntilNewline(true);
        FixedWidthParser parser = new FixedWidthParser(settings);

        for (int i = marks.get(JUNCTIONS + START); i <= marks.get(JUNCTIONS + END); i++) {
            String[] thisline = parser.parseLine(inpFileArray.get(i));
            boolean aponded = !thisline[5].equals("0");
            Junction junction = new Junction(thisline[0], Double.parseDouble(thisline[1]), Double.parseDouble(thisline[2]), Double.parseDouble(thisline[3]), Double.parseDouble(thisline[4]), aponded);
            junctions.put(thisline[0], junction);
        }
    }

    private void readOutfalls() {

        FixedWidthFields fwf = new FixedWidthFields(lengths.get(OUTFALLS));
        FixedWidthParserSettings settings = new FixedWidthParserSettings(fwf);
        settings.setRecordEndsOnNewline(true);
        settings.setSkipTrailingCharsUntilNewline(true);
        FixedWidthParser parser = new FixedWidthParser(settings);

        for (int i = marks.get(OUTFALLS + START); i <= marks.get(OUTFALLS + END); i++) {
            String[] thisline = parser.parseLine(inpFileArray.get(i));
            boolean gated = !thisline[4].equals("NO");
            Outfall outfall = new Outfall(thisline[0], Double.parseDouble(thisline[1]), thisline[2], thisline[3], gated, thisline[5]);
            outfalls.put(thisline[0], outfall);
        }
    }

    private void readConduits() {

        FixedWidthFields fwf = new FixedWidthFields(lengths.get(CONDUITS));
        FixedWidthParserSettings settings = new FixedWidthParserSettings(fwf);
        settings.setRecordEndsOnNewline(true);
        settings.setSkipTrailingCharsUntilNewline(true);
        FixedWidthParser parser = new FixedWidthParser(settings);

        for (int i = marks.get(CONDUITS + START); i <= marks.get(CONDUITS + END); i++) {
            String[] thisline = parser.parseLine(inpFileArray.get(i));
            Conduit conduit = new Conduit(thisline[0], thisline[1], thisline[2], Double.parseDouble(thisline[3]), Double.parseDouble(thisline[4]), Double.parseDouble(thisline[5]), Double.parseDouble(thisline[6]), Double.parseDouble(thisline[7]), Double.parseDouble(thisline[8]));
            conduits.put(thisline[0], conduit);
        }

        fwf = new FixedWidthFields(lengths.get(OUTFALLS));
        settings = new FixedWidthParserSettings(fwf);
        settings.setRecordEndsOnNewline(true);
        settings.setSkipTrailingCharsUntilNewline(true);
        parser = new FixedWidthParser(settings);

        for (int i = marks.get(XSECTIONS + START); i <= marks.get(XSECTIONS + END); i++) {
            String[] thisline = parser.parseLine(inpFileArray.get(i));
            Conduit conduit = conduits.get(thisline[0]);
            conduit.setShape(thisline[1]);
            conduit.setGeom1(Double.parseDouble(thisline[2]));
            conduit.setGeom2(Double.parseDouble(thisline[3]));
            conduit.setGeom3(Double.parseDouble(thisline[4]));
            conduit.setGeom4(Double.parseDouble(thisline[5]));
            conduits.put(thisline[0], conduit);
        }
    }

    public static File getINPFile(File inpFile) {
        JFileChooser chooser = new JFileChooser("inp");
        if (inpFile != null) {
            String path = inpFile.getAbsolutePath();
            String newPath = path.substring(0, path.length() - 4) + ".inp";
            chooser.setSelectedFile(new File(newPath));
        }
        chooser.setDialogTitle("Select SWMM input file");
        int returnval = chooser.showOpenDialog(null);
        if (returnval != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        File newFile = chooser.getSelectedFile();
        return newFile;
    }

    private static String getNewShapeFolder(File csvFile) {
        String path = csvFile.getAbsolutePath();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select folder for shapefiles");
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int returnval = chooser.showSaveDialog(null);
        if (returnval != JFileDataStoreChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        File newFile = chooser.getSelectedFile();
        return newFile.getAbsolutePath();
    }

    private String[] splitTrimmedLine(String line) {
        return line.trim().replaceAll("\\s+", " ").split(" ");
    }
}
