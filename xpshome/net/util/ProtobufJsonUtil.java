package xpshome.net.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;

/**
 * Created by Christian Poschinger on 17.02.2016.
 */
public class ProtobufJsonUtil {
    private static final String T_LINE = "LineString";
    private static final String T_POINT = "Point";
    private static final String T_POLY = "Polygon";
    private static final String COORDINATES = "coordinates";
    private static final String GEOMETRY = "geometry";
    private static final String FEATURES = "features";
    private static final String TYPE = "type";
    private static final String PROP_LAT = "lat";
    private static final String PROP_LON = "lon";

    public static boolean parseJson(String json, com.google.protobuf.Message.Builder builder) {
        try {
            com.google.protobuf.util.JsonFormat.parser().merge(json, builder);
            return true;
        } catch (Exception ex) {
            // TODO : logging
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean parseGeoJson(String geoJson, com.google.protobuf.Message.Builder builder) {
        try {
            com.google.protobuf.util.JsonFormat.parser().merge(
                    ProtobufJsonUtil.geoJsonToProtobufJson(geoJson),
                    builder);

            return true;
        } catch (Exception ex) {
            // TODO : logging
            ex.printStackTrace();
        }
        return false;
    }

    public static String geoJsonToProtobufJson(String geojson) {
        JsonReader reader = new JsonReader(new StringReader(geojson));
        reader.setLenient(false);
        try {
            JsonObject object = (JsonObject) parse(reader);
            JsonArray feat_el = object.getAsJsonArray(FEATURES);
            for (JsonElement el : feat_el) {
                formatCoordinates(((JsonObject) el).getAsJsonObject(GEOMETRY));
            }

            return object.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static void formatCoordinates(JsonObject geometry) {
        JsonArray coordarray = geometry.getAsJsonArray(COORDINATES);
        switch (geometry.get(TYPE).getAsString()) {
            case T_POINT : {
                JsonObject pt = arrayToObject(coordarray);
                coordarray.set(0, pt);
                coordarray.remove(1);
                break;
            }
            case T_POLY : {
                JsonArray p = parseMultiPoints((JsonArray)coordarray.get(0));
                geometry.remove(COORDINATES);
                geometry.add(COORDINATES, p);
                break;
            }
            case T_LINE : {
                JsonArray p = parseMultiPoints(coordarray);
                geometry.remove(COORDINATES);
                geometry.add(COORDINATES, p);
                break;
            }
        }
    }

    private static JsonArray parseMultiPoints(JsonArray points) {
        JsonArray ret = new JsonArray();
        for (int i = 0; i < points.size(); i++) {
            ret.add(arrayToObject((JsonArray) points.get(i)));
        }
        return ret;
    }

    private static JsonObject arrayToObject(JsonArray point) {
        JsonObject jb = new JsonObject();
        jb.addProperty(PROP_LAT, point.get(0).getAsDouble());
        jb.addProperty(PROP_LON, point.get(1).getAsDouble());
        return jb;
    }

    private static JsonElement parse(JsonReader json) throws JsonIOException, JsonSyntaxException {
        boolean lenient = json.isLenient();
        json.setLenient(true);
        try {
            return Streams.parse(json);
        } catch (StackOverflowError e) {
            throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
        } catch (OutOfMemoryError e) {
            throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
        } finally {
            json.setLenient(lenient);
        }
    }
}
