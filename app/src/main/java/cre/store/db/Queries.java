package cre.store.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class Queries {

	private final static String SQL_FILE_PREFIX = "/store/db/sql/"; 

	public static String sqlDialect = null;

	static public String getQuery(String name) {
    	try {
			return new String(Queries.class.getResourceAsStream(SQL_FILE_PREFIX + name).readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
    }

	static public List<String> getQuery(String name, String section) {
        Yaml yaml = new Yaml();
        InputStream inputStream = Queries.class.getResourceAsStream("/db/" + name + ".sql.yaml"); 
        Map<String, Object> obj = yaml.load(inputStream);
		        
		List<String> result = new ArrayList<String>();
		for (Object line: (List<Object>) obj.get(section)) {
			if (line instanceof String) {
				result.add((String) line);
			} else if (line instanceof Map) {
				String value = (String) ((Map) line).get(sqlDialect);
				if (value != null) {
					result.add(value);
				}
			}

		}
		
		return result;
    }
}