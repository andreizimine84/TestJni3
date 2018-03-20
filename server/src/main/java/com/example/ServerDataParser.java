package com.example;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
// ta bort keyargv
// ta bort kb
//  key tom ta bort
// text till situationstecken via exception
// kolla om det är int annars sätta situationstecken
public class ServerDataParser {
	private static ArrayList<File> pathsArgv = new ArrayList<File>();
	private static boolean titleAdded = false;
	ServerDataParser sdp = null;
	private static char ASCII_SOT = '\002';
	private static char ASCII_TE = '\003';
	private static char ASCII_US = '\037';
	private static char ASCII_RS = '\030';
	private static char ASCII_EOT = '\004';
    public static void main(String argv[]) throws IOException {
        String keyArgv = null;
        String titleArgv = null;
        String outputArgv = null;
		ServerDataParser sdp = new ServerDataParser();
		for(int i = 0; i < argv.length; i++){
			if(argv[i].startsWith("-key")){
				keyArgv = argv[i + 1];
				i++;
			}
			else if(argv[i].startsWith("-title")){
				titleArgv = argv[i + 1];
				i++;
			}
            else if(argv[i].startsWith("-output")){
                outputArgv = argv[i + 1];
				pathsArgv.add(new File(outputArgv));
                i++;
            }
			else{
                String glob;
					Path path = null;
					if(argv[i].startsWith("glob")) {
						glob = argv[i];
					String pathArgv = System.getProperty("user.dir");
					match(glob, pathArgv);
				}
				if(argv[i].endsWith(".txt") && !argv[i].startsWith("glob")) {
					if (argv[i].matches("(.*)*(.*)")) {
						String pathArgv = System.getProperty("user.dir");
						match("glob:**/" + argv[i], pathArgv);
					}
				}
				else if(new File (argv[i]).exists() && !argv[i].contains(System.getProperty("user.dir"))){
						pathsArgv.add(new File(argv[i]));
					}
					try {
						path = Paths.get(argv[i]);
				}
				catch(InvalidPathException ipe){

				}
				if(isDir(path)){
					try {
						pathsArgv.addAll(sdp.listByFiles(path.toString()));
					}
					catch(NullPointerException ne){

					}
				}
				if(argv[i].contains(System.getProperty("user.dir"))) {
					pathsArgv.add(new File(argv[i]));
				}
			}
		}
    for (File file : pathsArgv) {
		if(keyArgv != null && titleArgv != null && outputArgv != null) {
			sdp.readCompleteFileByTitle(file.getAbsolutePath(), keyArgv, titleArgv, outputArgv);
		}
		else if(keyArgv == null && titleArgv != null && outputArgv != null){
			sdp.readCompleteFileByTitle(file.getAbsolutePath(), null, titleArgv, outputArgv);
		}
		else if(keyArgv == null && titleArgv == null && outputArgv == null){
			sdp.readCompleteFileByTitle(file.getAbsolutePath(), null, null, "");
		}
		else{
			sdp.readCompleteFileByTitle(file.getAbsolutePath(), keyArgv, titleArgv, outputArgv);
		}
    }
}
    public static void match(String glob, String location) throws IOException {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);

        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {

            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    pathsArgv.add(path.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }


	public String getNextBlock(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int singleChar;
		String value;

		while((singleChar = input.read()) != -1){
			value = String.valueOf((char)singleChar);
			baos.write(value.getBytes());
			if(singleChar == ASCII_SOT)
				break;
			if(singleChar == ASCII_EOT)
				return baos.toString();	
		}
		if(singleChar == -1)
			return null;
		
		return baos.toString();
	}

	private String getTitleFromBlock(String block) {
		StringReader reader = new StringReader(block);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int singleChar;
		String stringValueOf;
		try {
			while ((singleChar = reader.read()) != -1) {
				stringValueOf = String.valueOf((char) singleChar);
				baos.write(stringValueOf.getBytes());
				if (singleChar == ASCII_TE) {
					return baos.toString().trim();
				}
			}
		}
		catch(IOException io){
			io.printStackTrace();
		}
		return "";
	}

	public LinkedHashMap<String,String> getKeyValues(String block) {
		LinkedHashMap<String, String> keyValueFinal = new LinkedHashMap<String, String>();
		try {
			Iterator<String> keyValue2Iterator = getKeys(block).iterator();
			Iterator<String> keyValueIterator = getValues(block).iterator();
			while (keyValue2Iterator.hasNext() && keyValueIterator.hasNext()) {
				keyValueFinal.put(keyValue2Iterator.next(), keyValueIterator.next());
			}
			return keyValueFinal;
		}
		catch(IOException io){
			io.printStackTrace();
		}
		return keyValueFinal;
	}
	
	private List<String> getKeys(String block) throws IOException{
		StringReader reader = new StringReader(block);
		int singleChar;
		String value;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		List<String> keyValue = new ArrayList<String>();
		while((singleChar = reader.read()) != -1){	
			value = String.valueOf((char)singleChar);
			baos.write(value.getBytes()); 
			if(singleChar == ASCII_TE){
				baos.reset();
			}
			if(singleChar == ASCII_US){
				keyValue.add(baos.toString());
				baos.reset();
				while((singleChar = reader.read()) != -1){
					value = String.valueOf((char)singleChar);
					baos.write(value.getBytes());
					if(singleChar == ASCII_RS){	
						baos.reset();
						break;
					}
				}
			}
		}
		return keyValue;
	}
	
	private List<String> getValues(String block) throws IOException{
		StringReader reader = new StringReader(block);
		int singleChar;
		String value;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		List<String> keyValue = new ArrayList<String>();
		while((singleChar = reader.read()) != -1){
			value = String.valueOf((char)singleChar);
			baos.write(value.getBytes());
			if(singleChar == ASCII_US){
				baos.reset();
				while((singleChar = reader.read()) != -1){
					value = String.valueOf((char)singleChar);
					baos.write(value.getBytes());
					if(singleChar == ASCII_RS){
						keyValue.add(baos.toString());
						baos.reset();
						break;
					}
				}
			}
		}
		return keyValue;
	}

	private void writeFileNameToFile(String key, String output){
		File file = null;
		String[] split = output.split("/");
		if(output.endsWith(".json") && output.contains("/"))
			file= new File (split[0] + "/" + "fileNames.txt");
		else
			file= new File ("fileNames.txt");
		InputStreamReader inputStreamReader;
		BufferedReader bufferedReader;
		String line;
		boolean lineCheck = false;
		try {
			FileWriter writer;
			if (file.exists())
			{
				inputStreamReader = new InputStreamReader(new FileInputStream (file));
                bufferedReader = new BufferedReader(inputStreamReader);
                writer = new FileWriter(file, true);
				while((line = bufferedReader.readLine()) != null){
					if(Objects.equals(line, key)){
						lineCheck = true;
						break;
					}
				}
				if(!lineCheck){
					writer.write(key);
					writer.write(System.getProperty("line.separator"));
				}
				writer.close();
				inputStreamReader.close();
				bufferedReader.close();
       	 	}
			else if(!file.exists()){
				if(file.createNewFile()) {
					inputStreamReader = new InputStreamReader(new FileInputStream(file));
					bufferedReader = new BufferedReader(inputStreamReader);
					writer = new FileWriter(file, true);
					while ((line = bufferedReader.readLine()) != null) {
						if (Objects.equals(line, key)) {
							lineCheck = true;
							break;
						}
					}
					if (!lineCheck) {
						writer.write("Files");
						writer.write(System.getProperty("line.separator"));
						writer.write(key);
						writer.write(System.getProperty("line.separator"));
					}
					writer.close();
					inputStreamReader.close();
					bufferedReader.close();
				}
			}
		}
		catch(IOException io){
			io.printStackTrace();
		}
	}

		private void writeBlockToFile(String block, String key, String title, String output){
				File file = null;
				FileWriter writer = null;
				String titleReturn = getTitleFromBlock(block);
				try {
					if (!titleAdded && key != null) {
						if (key.equals(titleReturn)) {
							titleAdded = true;
							writeFileNameToFile(title ,output);
						}
					}
				}
				catch(NullPointerException io){
					io.printStackTrace();
				}
			LinkedHashMap<String, String> keyValues = getKeyValues(block);
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> entry : keyValues.entrySet()) {
					String keyReturn = entry.getKey();
					String valueReturn = entry.getValue();
						if (key == null || title.equals(titleReturn.trim())) {
							if(key == null && title == null) {
								System.out.println(titleReturn + "\t" + keyReturn.trim() + "\t" + valueReturn.trim());
							}
							else if (key != null && title != null && title.equals(titleReturn.trim()) && key.equals(keyReturn.trim())) {
								System.out.println(titleReturn + "\t" + keyReturn.trim() + "\t" + valueReturn.trim());
								try {
									writer = new FileWriter(titleReturn + "_" + keyReturn.trim() + ".json", true);
									file = new File(titleReturn + "_" + keyReturn.trim() + ".json");
								} catch (IOException io) {
									io.printStackTrace();
								}
							}
							if (key == null && title == null || output.endsWith("/") || output != null) {
								if (keyReturn.trim() != null && valueReturn.trim() != null && titleReturn != null) {
									try {
										if (output == null) {
											if (isValidFileName(titleReturn + "_" + keyReturn.trim() + ".json")) {
												writer = new FileWriter(titleReturn + "_" + keyReturn.trim() + ".json", true);
												file = new File(titleReturn + "_" + keyReturn.trim() + ".json");
												titleAdded = false;
											}
										} else {
											if (isValidFileName(titleReturn + "_" + keyReturn.trim() + ".json")) {
												if(output.endsWith("/")){
													writer = new FileWriter(output + titleReturn + "_" + keyReturn.trim() + ".json", true);
													file = new File(output + titleReturn + "_" + keyReturn.trim() + ".json");
												}
												else{
													writer = new FileWriter(titleReturn + "_" + keyReturn.trim() + ".json", true);
													file = new File(titleReturn + "_" + keyReturn.trim() + ".json");
												}
												titleAdded = false;
											}
										}
										valueReturn = valueReturn.replaceAll("\\P{Print}", "");

										if (valueReturn.compareTo("") == 0) {
											try {
												file.delete();
												file.deleteOnExit();
												writer.close();
												break;
											} catch (NullPointerException ne) {

											}
										}
										if(valueReturn.endsWith("kB")){
											valueReturn = valueReturn.substring(0, valueReturn.length() - 2);
										}
										if (keyReturn != null) {
											if(!valueReturn.matches(".*[a-z].*") || keyReturn.matches(".*[a-z].*")) {
												writeFileNameToFile(titleReturn.trim() + "_" + keyReturn.trim(), output);
												//titleAdded = true;
											}
											if(valueReturn.contains("false") || valueReturn.contains("true") || valueReturn.contains("Files")){
												writeFileNameToFile(titleReturn.trim() + "_" + keyReturn.trim(), output);
												//titleAdded = true;
											}
										}
							}
							catch(IOException io){
										io.printStackTrace();
									}
								}
							}
							if (valueReturn.length() >= 1 && writer != null || valueReturn.compareTo("0") == 0) {
									String[] parts = new String[2];
									parts[0] = keyReturn.trim();
									parts[1] = valueReturn.trim();
									try {
									if (parts.length >= 2 || valueReturn.compareTo("0") == 0) {

											InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
											BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
											String line = bufferedReader.readLine();
											boolean numeric = true;
											if (line == null && file.getAbsolutePath().contains(titleReturn.trim()) || line.length() == 0) {
												sb.append("[{" + "\"" + parts[0] + "\"" + " : [");
												numeric = parts[1].matches("-?\\d+(\\.\\d+)?");
												if(numeric == false && valueReturn.compareTo("0") != 0)
													sb.append('"' + parts[1] + '"');
												else if(numeric == true)
													sb.append(parts[1]);
												if(valueReturn.compareTo("0") == 0 && numeric != true)
													sb.append(0);
												if (sb.length() > 0) {
													sb.append("]");
													sb.append("}]");
												}
												writer.write(sb.toString().trim());
												writer.flush();
												sb.setLength(0);
											}

											if(line != null) {
												if (line.contains("[{")) {
													keyReturn = keyReturn.replaceAll("\\P{Print}", "");
													String [] split = output.split("/");
													writer = new FileWriter(titleReturn.trim() + "_" + keyReturn.trim() + ".json");

													if (output != null) {
														if (output.endsWith(".json"))
															writer = new FileWriter(split[0] + "/" + titleReturn.trim() + "_" + keyReturn.trim() + ".json", true);
													} else if (output != null) {
														if (!output.endsWith(".json"))
															writer = new FileWriter(output + titleReturn + "_" + keyReturn.trim() + ".json", true);
													} else {
														writer = new FileWriter(output + titleReturn + "_" + keyReturn.trim() + ".json", true);
													}
													numeric = parts[1].matches("-?\\d+(\\.\\d+)?");
													if(numeric == false)
														line = line.replace("]}]", "," + '"' + parts[1] + '"');
													else if(numeric == true)
														line = line.replace("]}]", "," + parts[1]);
													writer.write(line);
													writer.write("]");
													writer.write("}]");
													writer.flush();
												}
											}
											bufferedReader.close();
										}
										if (writer != null) {
											writer.close();
										}
									}
									catch(IOException io){
										io.printStackTrace();
									}
									catch(NullPointerException ne){
										ne.printStackTrace();
									}
								}
							}
						}
				}

	private boolean isValidFileName(final String aFileName) {
		final File aFile = new File(aFileName);
		boolean isValid = true;
		try {
			if (aFile.createNewFile()) {
				aFile.delete();
			}
		} catch (IOException e) {
			isValid = false;
		}
		return isValid;
	}

	private void readCompleteFileByTitle(String fileName, String key, String title, String filePath)  {
		InputStream is;
		try {
			is = new FileInputStream(fileName);
			sdp = new ServerDataParser();
			String block = sdp.getNextBlock(is);
                    while (block != null) {
                        //if(block != null && key != null && title != null)
					    	sdp.writeBlockToFile(block, key, title, filePath);
						//else if(key == null && title == null)
						//	sdp.writeBlockToFile(block, key, title, output, filePath);
                    block = sdp.getNextBlock(is);
                }
    } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<File> listByFiles(String directory){
		File f;
	    File[] paths;
	    ArrayList<File> rPaths = new ArrayList<File>();
	    
	    ServerDataParser sdp = new ServerDataParser();
	    try{
                f = new File(directory);
                paths = f.listFiles();
                for(File path:paths)
                {
	        	if(path.getAbsolutePath().endsWith(".txt")){
	        		rPaths.add(path);
	        	}
	        	else if(path.isDirectory())
	        	{
	        		sdp.listByFiles(path.getAbsolutePath());
	        	}
	        }
	      }
	      catch(Exception e){
	    	  e.printStackTrace();
		  }
		return rPaths;
	}
	
	static Boolean isDir(Path path) {
		  if (path == null || !Files.exists(path)) 
			  return false;
		  else 
			  return Files.isDirectory(path);
	}
}
