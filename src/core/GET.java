package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * @author Vincent Boulanger
 *
 */
public class GET {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Variable for user input, by parameter or direct keyboard input
		String cityName = "";
		
		// Boolean variable, to allow exit while direct keyboard input
		boolean continueProgram = true;

		// If an argument has been provided, we store it in the variable slot
		if (args.length > 0) {
			cityName = args[0];
		} else {
			// Otherwise, we ask user for a value, or "exit" to quit the program
			Scanner keyboard = new Scanner(System.in);
			while (cityName.length() == 0) {
				System.out.println("This command works with one string argument. Please provide it now or write exit to quit.");
				cityName = keyboard.nextLine();
				if (cityName.equals("exit")) {
					continueProgram = false;
				}
			}
			keyboard.close();
		}

		// If a value, other than "exit", we can proceed.
		if (continueProgram) {
			// URL that will be used to request the JSON data,
			// the user given value is added at the end.
			String urlRequest = "http://api.goeuro.com/api/v2/position/suggest/en/" + cityName;

			// The name of the CSV file used to write parsed data, it's locally created,
			// in running directory
			String fileName = "./result.csv";

			// The whole data to write, starting with header, then completed further,
			// before writing
			String dataToWrite = "id;name;type;latitude;longitude\n";

			try {
				// Connection to the URL and query for JSON data
				URL url = new URL(urlRequest);
				InputStream is = url.openStream();
				JsonReader r = Json.createReader(is);
				JsonArray a = r.readArray();

				// Test for the result, if no result, then a message is shown
				// then empty data string
				if (a.size() == 0) {
					dataToWrite = null;
				} else {
					// Printing number of results
					System.out.println(a.size() + " result(s) for " + cityName + ". Processing file...");

					// Parsing JSON data
					Iterator<JsonValue> it = a.iterator();
					while (it.hasNext()) {
						// Collect various data
						JsonObject mainObject = (JsonObject) it.next();
						JsonObject subObject = mainObject.getJsonObject("geo_position");

						// Build line with values and append it to existing data
						// waiting to be written.
						dataToWrite += mainObject.getInt("_id") + ";" + mainObject.getString("name") + ";"
								+ mainObject.getString("type") + ";" + subObject.getJsonNumber("latitude") + ";"
								+ subObject.getJsonNumber("longitude") + "\n";
					}
				}
			} catch (MalformedURLException e) {
				System.out.println("[X] Query URL is not correctly formed, perhaps the city name is mistyped.\nURL:" + urlRequest);
				System.out.println("End of program, no file created.");
				System.exit(1);
			} catch (IOException e) {
				System.out.println("[X] Unable to open stream toward URL.\nURL:" + urlRequest);
				System.out.println("End of program, no file created.");
				System.exit(2);
			}

			try {
				if (dataToWrite != null) {
					// Creation of the file handler, filewriter, and buffer
					File resultFile = new File(fileName);
					FileWriter fw = new FileWriter(resultFile);
					BufferedWriter bw = new BufferedWriter(fw);

					// Writing of complete data, flush and close file
					bw.write(dataToWrite);
					bw.flush();
					bw.close();

					System.out.println("[V] File written:" + resultFile.getAbsolutePath());
				} else {
					System.out.println("[V] 0 result for " + cityName + ". No file created.");
				}
			} catch (IOException e) {
				System.out.println("[X] Unable to write the file.\nFilename:" + fileName);
				System.out.println("End of program, no file created.");
				System.exit(3);
			}
		} else {
			// Message to remind that user asked to quit.
			System.out.println("Exit asked by user, no file created.");
		}

		// When reaching this point, everything is finished, messages have been written
		// We can exit properly (code 0)
		System.out.println("End of program.");
		System.exit(0);
	}
}
