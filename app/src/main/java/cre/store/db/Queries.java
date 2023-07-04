package cre.store.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class Queries {

	private final static String SQL_FILE_PREFIX = "/store/db/sql/"; 


	static public String getQuery(String name) {
    	try {
			return new String(Queries.class.getResourceAsStream(SQL_FILE_PREFIX + name).readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
    }

	static public String getQuery(String name, String section) {
        Yaml yaml = new Yaml();
        InputStream inputStream = Queries.class.getResourceAsStream("/db/" + name + ".yaml.sql"); 
        Map<String, String> obj = yaml.load(inputStream);
        return obj.get(section);
    }
}