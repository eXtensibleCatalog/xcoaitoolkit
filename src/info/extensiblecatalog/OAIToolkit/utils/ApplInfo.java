/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package info.extensiblecatalog.OAIToolkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import info.extensiblecatalog.OAIToolkit.importer.ImporterConfiguration;
import info.extensiblecatalog.OAIToolkit.DTOs.DataTransferObject;
import info.extensiblecatalog.OAIToolkit.DTOs.SetDTO;
import info.extensiblecatalog.OAIToolkit.configuration.OAIConfiguration;
import info.extensiblecatalog.OAIToolkit.db.DButil;
import info.extensiblecatalog.OAIToolkit.db.LuceneSearcher;
import info.extensiblecatalog.OAIToolkit.db.managers.SetsMgr;
import info.extensiblecatalog.OAIToolkit.oai.MetadataFormatUnmarshaler;
import info.extensiblecatalog.OAIToolkit.oai.MetadataFormats;
import info.extensiblecatalog.OAIToolkit.oai.StorageTypes;
import info.extensiblecatalog.OAIToolkit.utils.XcOaiIdConfigUtil;

/**
 * Basic informations about the application (directories, properties etc.)
 * @author Péter Király
 */
public class ApplInfo {
	
        private static String programmer_log = "programmer";
        private static final Logger prglog = Logging.getLogger(programmer_log);
    
	//private static final Logger logger = Logging.getLogger();
        	
	/** The OAI configuration file. Contains the configuration values
	 * for serving the OAI requests. See {@link #oaiConf}, 
	 * {@link OAIConfiguration} */
	private static final String OAI_SERVER_CNF_FILE = 
										"OAIToolkit.server.properties";
	
	/** Configuration file for logging */
	private static final String OAI_LOG4J_CNF_FILE = 
										"OAIToolkit.log4j.properties";
	
	/** Configuration file for database */
	private static final String OAI_DB_CNF_FILE = 
											"OAIToolkit.db.properties";
	
	/** Configuration file for directory */
	private static final String OAI_DIRECTORY_CNF_FILE = 
									"OAIToolkit.directory.properties";

    /** Configuration file for XC OAI ID structure */
    private static final String OAI_XCOAIID_CNF_FILE =
									"OAIToolkit.xcoaiid.properties";

	/** The log directory's name */
	private static String logDir;
	
	/** The directory, where the (mainly xsl) files are stored outside of
	 * the web application */
	private static String resourceDir;

    /** The Web Application version name running */
	private static String applVer;
	
	/** We can filter records containing only this organization code
	 * Useful if you want to use a single repo for an amalgamation of records, but also
	 * allowing other OAIToolkit instances the ability to share only a subset of it (based on orgCode)
	 */
	private static String orgCodeFilter = null;
	private static String orgCodeFilterXlateOaiIdentifierFrom = null;
	private static String orgCodeFilterXlateOaiIdentifierTo = null;

	/** The directory, where the (mainly xsl) files are stored outside of
	 * the web application */
	public static LuceneSearcher luceneSearcher;
	
	/** The OAI configuration object. This object handles the
	 * storing and loading configuration values from the 
	 * {@link #oaiConfigFileName} */
	public static OAIConfiguration oaiConf;
	
	/** The object storing and loading the metadata formats used
	 * by the ListMetadataformats verb and metadataPrefix parameter */
	public static MetadataFormats metadataFormats;
	
	/** The Tomcat's bin directory. This is the place of some
	 * config files. */
	public static File tomcatBinDir;
	
	/** A hash table for caching set names by its id */
	public static final Map<Integer, String> setNamesById = 
		new HashMap<Integer, String>();

	/** A hash table for caching set ids by its name */
	public static final Map<String, Integer> setIdsByName = 
		new HashMap<String, Integer>();
	
	/** The line separator */
	public static final String LN = System.getProperty("line.separator");
	
	/** The cache directory */
	public static File cacheDirectory;

    /** List of error messages */
	// TODO: write out error messages in the index page
	public static final List<String> errorMessages = new ArrayList<String>();
	
	/** possible state of a cached request */
	public enum RequestState{ STARTED, FINISHED };

	/** state of the cached request */
	public static Map<String, RequestState> cacheRegister 
			= new HashMap<String, RequestState>();
	
	/** the timestamp of the last cache clearance 
	 * -1 means that there haven't been cache clearance yet */
	public static long lastCacheClear = -1;
	
	/** The minimum interval between two cache clearence */
	public static long cacheClearInterval = 5 * 60 * 1000;


	/** initialize the command line interface */
	public static void init(String logDir) throws Exception {
		//initApplication();
		System.out.println("init(logDir): " + logDir);
		initLogging(logDir);
		initDB(logDir);
        initXcOaiId(logDir);
		initSets();
	}

	public static void init(String rootDir, String logDir) throws Exception {
		//initApplication();
		initLogging(rootDir, logDir);
		initDB(rootDir);
        initXcOaiId(rootDir);
		initSets();
	}

	/** initialize the OAI server */
	public static void init(File binDir, String webAppName) throws Exception {
		System.out.println("ApplInfo::init(" + binDir + ")");
		tomcatBinDir = binDir;
        System.out.println("ApplInfo::init(" + webAppName + ")");
        applVer = webAppName;
		initApplication();
	}
	
	
    /** initialize the command line interface for the Lucene Statistics */
	public static void statsInit(String rootDir, String logDir) throws Exception {
		initLogging(rootDir, logDir);
	}

	/**
	 * Initialize the database setup
	 * @throws Exception
	 */
	public static void initDB() throws Exception {
		if(null != tomcatBinDir) {
			DButil.init(new File(tomcatBinDir, OAI_DB_CNF_FILE).getAbsolutePath());
		} else {
			DButil.init(OAI_DB_CNF_FILE);
		}
	}

	public static void initDB(String dir) throws Exception {
		DButil.init(new File(dir, OAI_DB_CNF_FILE).getAbsolutePath());
	}

    /**
     * Initialize the OAIToolkit.oaiid.properties file
     * @param dir
     * @throws java.lang.Exception
     */

    public static void initXcOaiId(String dir) throws Exception {
        XcOaiIdConfigUtil.init(new File(dir, OAI_XCOAIID_CNF_FILE).getAbsolutePath());
    }

	/**
	 * start logging
	 */
	public static void initLogging(String logDir) throws FileNotFoundException {
		initLogging(logDir, logDir);
	}

	/**
	 * start logging
	 */
	public static void initLogging(String rootDir, String logDir) throws FileNotFoundException {
		initLogging(rootDir, logDir, OAI_LOG4J_CNF_FILE);
	}

	/**
	 * start logging
	 */
	public static void initLogging(String rootDir, String logDir, 
			String propertyFileName) 
			throws FileNotFoundException {
		ClassLoader cloader = ApplInfo.class.getClassLoader();
		InputStream logProps = cloader.getResourceAsStream(propertyFileName);
		if(null == logProps) {
			File propertyFile = new File(rootDir, propertyFileName);
			if(propertyFile.exists()) {
				try {
					System.out.println(" log4j property file: "
							+ propertyFile.getAbsoluteFile());
					logProps = new FileInputStream(propertyFile);
				} catch(FileNotFoundException e) {
					System.out.println("Exception" + e);
					throw e;
				}
			}
		}
		if(null != logProps) {
			LoggingParameters logParams = new LoggingParameters(logDir);
			Logging.initLogging(logParams, logProps);
		}
	}
	
	/**
	 * Initialize the application. Setup {@link #logDir}, {@link #clone(),
	 * loading {@link #oaiConf} and {@link #metadataFormats}
	 * @throws Exception
	 */
	public static void initApplication() throws Exception {
		try {
			// aplication properties
			System.out.println("reading application properties");
			String basePropertiesFileName = OAI_DIRECTORY_CNF_FILE;
			File oaiConfigFile = null;
			String configDirName = null;
			if(null != tomcatBinDir) {
				basePropertiesFileName = new File(tomcatBinDir, 
						basePropertiesFileName).getAbsolutePath();
	            configDirName = tomcatBinDir.getAbsolutePath(); // default

			}

			System.out.println("basePropertiesFileName: " + basePropertiesFileName);

			PropertiesConfiguration applConf = ConfigUtil
					.load(basePropertiesFileName);

            String luceneDir = null;
            String cacheDir = null;

            if(applVer.equals("OAIToolkit")) {
            	// allow alternate config dir
            	configDirName = applConf.getString("configDir", configDirName);
            	
              	logDir = applConf.getString("logDir");
                logDir = logDir.replaceAll("\\\\+","/");
                System.out.println("ApplInfo::logDir: " + logDir);
                resourceDir = applConf.getString("resourceDir");
                resourceDir = resourceDir.replaceAll("\\\\+", "/");
                System.out.println("ApplInfo::resourceDir: " + resourceDir);
                luceneDir = applConf.getString("luceneDir");
                luceneDir = luceneDir.replaceAll("\\\\+", "/");
                System.out.println("ApplInfo::luceneDir: " + luceneDir);
                cacheDir = applConf.getString("cacheDir");
                cacheDir = cacheDir.replaceAll("\\\\+", "/");
                System.out.println("ApplInfo::cacheDir: " + cacheDir);
                
            }
            else {
            	// alternate config dir is NECESSARY when running multiple instances!
            	configDirName = applConf.getString(applVer + "." + "configDir");

                String versionedLogDir = applVer + "." + "logDir";
                logDir = applConf.getString(versionedLogDir);
                logDir = logDir.replaceAll("\\\\+","/");
                System.out.println("ApplInfo::logDir: " + logDir);
                String versionedResourceDir = applVer + "." + "resourceDir";
                resourceDir = applConf.getString(versionedResourceDir);
                resourceDir = resourceDir.replaceAll("\\\\+", "/");
                System.out.println("ApplInfo::resourceDir: " + resourceDir);
                String versionedLuceneDir = applVer + "." + "luceneDir";
                luceneDir = applConf.getString(versionedLuceneDir);
                luceneDir = luceneDir.replaceAll("\\\\+", "/");
                System.out.println("ApplInfo::luceneDir: " + luceneDir);
                String versionedCacheDir = applVer + "." + "cacheDir";
                cacheDir = applConf.getString(versionedCacheDir);
                cacheDir = cacheDir.replaceAll("\\\\+", "/");
                System.out.println("ApplInfo::cacheDir: " + cacheDir);
                
            }


            if(cacheDir == null) {
				errorMessages.add("There is no 'cacheDir' defined in " 
						+ basePropertiesFileName);
			} else {
				File cache = new File(cacheDir);
				if(!cache.exists()){
					errorMessages.add("The cache directory ("+ cacheDir 
							+ ") does not exist. The tool will create it.");
					boolean created = cache.mkdir();
				} else {
					if(cache.isDirectory()) {
						cacheDirectory = cache;
					} else {
						errorMessages.add("The cache directory ("+ cacheDir 
								+ ") is not directory.");
					}
				}
			}
			
			// oai server configuration
			prglog.trace("[PRG] reading OAI properties");
			
			oaiConfigFile = new File(configDirName + "/" + OAI_SERVER_CNF_FILE);			
			System.out.println("oaiConfigFile: " + oaiConfigFile);
						
			if(null != oaiConfigFile) {
				prglog.info("[PRG] OAIConfiguration with oaiConfigFile: " 
						+ oaiConfigFile);
				oaiConf = new OAIConfiguration(oaiConfigFile);
			} else {
				prglog.info("[PRG] OAIConfiguration with oaiConfigFileName: " 
						+ OAI_SERVER_CNF_FILE);
				oaiConf = new OAIConfiguration(OAI_SERVER_CNF_FILE);
			}
			oaiConf.load();
			
            // these settings are mainly used for supporting orgCode-based (subset) repositories
            if(applVer.equals("OAIToolkit")) {
	            orgCodeFilter = applConf.getString("orgCodeFilter");
	            System.out.println("ApplInfo::orgCodeFilter: " + orgCodeFilter);
	            orgCodeFilterXlateOaiIdentifierFrom = applConf.getString("orgCodeFilterXlateOaiIdentifierFrom");
	            System.out.println("ApplInfo::orgCodeFilterXlateOaiIdentifierFrom: " + orgCodeFilterXlateOaiIdentifierFrom);
	            orgCodeFilterXlateOaiIdentifierTo = applConf.getString("orgCodeFilterXlateOaiIdentifierTo");
	            System.out.println("ApplInfo::orgCodeFilterXlateOaiIdentifierTo: " + orgCodeFilterXlateOaiIdentifierTo);

	            String baseUrl = applConf.getString("baseUrl", "");
	            if (! baseUrl.equals("")) {
	                System.out.println("ApplInfo.oaiConf::baseUrl: " + baseUrl);
	            	oaiConf.setBaseUrl(baseUrl);
	            }
	            String oaiIdentifierScheme = applConf.getString("oaiIdentifierScheme", "");
	            if (! oaiIdentifierScheme.equals("")) {
	                System.out.println("ApplInfo.oaiConf::oaiIdentifierScheme: " + oaiIdentifierScheme);
	            	oaiConf.setOaiIdentifierScheme(oaiIdentifierScheme);
	            }
	            String repositoryName = applConf.getString("repositoryName", "");
	            if (! repositoryName.equals("")) {
	                System.out.println("ApplInfo.oaiConf::repositoryName: " + repositoryName);
	            	oaiConf.setRepositoryName(repositoryName);
	            }
	            String oaiIdentifierDelimiter = applConf.getString("oaiIdentifierDelimiter", "");
	            if (! oaiIdentifierDelimiter.equals("")) {
	                System.out.println("ApplInfo.oaiConf::oaiIdentifierDelimiter: " + oaiIdentifierDelimiter);
	            	oaiConf.setOaiIdentifierDelimiter(oaiIdentifierDelimiter);
	            }
	            String oaiIdentifierRepositoryIdentifier = applConf.getString("oaiIdentifierRepositoryIdentifier", "");
	            if (! oaiIdentifierRepositoryIdentifier.equals("")) {
	                System.out.println("ApplInfo.oaiConf::oaiIdentifierRepositoryIdentifier: " + oaiIdentifierRepositoryIdentifier);
	            	oaiConf.setOaiIdentifierRepositoryIdentifier(oaiIdentifierRepositoryIdentifier);
	            }
	            String oaiIdentifierSampleIdentifier = applConf.getString("oaiIdentifierSampleIdentifier", "");
	            if (! oaiIdentifierSampleIdentifier.equals("")) {
	                System.out.println("ApplInfo.oaiConf::oaiIdentifierSampleIdentifier: " + oaiIdentifierSampleIdentifier);
	            	oaiConf.setOaiIdentifierSampleIdentifier(oaiIdentifierSampleIdentifier);
	            }
	            String description = applConf.getString("description", "");
	            if (! description.equals("")) {
	                System.out.println("ApplInfo.oaiConf::description: " + description);
	            	oaiConf.setDescription(description);
	            }
            } else {
                String versionedOrgCodeFilter = applVer + "." + "orgCodeFilter";
                orgCodeFilter = applConf.getString(versionedOrgCodeFilter);
                System.out.println("ApplInfo::orgCodeFilter: " + orgCodeFilter);
	            orgCodeFilterXlateOaiIdentifierFrom = applConf.getString(applVer + "." + "orgCodeFilterXlateOaiIdentifierFrom");
	            System.out.println("ApplInfo::orgCodeFilterXlateOaiIdentifierFrom: " + orgCodeFilterXlateOaiIdentifierFrom);
	            orgCodeFilterXlateOaiIdentifierTo = applConf.getString(applVer + "." + "orgCodeFilterXlateOaiIdentifierTo");
	            System.out.println("ApplInfo::orgCodeFilterXlateOaiIdentifierTo: " + orgCodeFilterXlateOaiIdentifierTo);
                
                String baseUrl = applConf.getString(applVer + "." + "baseUrl", "");
                if (! baseUrl.equals("")) {
                    System.out.println("ApplInfo.oaiConf::baseUrl: " + baseUrl);
                	oaiConf.setBaseUrl(baseUrl);
                }
                String oaiIdentifierScheme = applConf.getString(applVer + "." + "oaiIdentifierScheme", "");
                if (! oaiIdentifierScheme.equals("")) {
                    System.out.println("ApplInfo.oaiConf::oaiIdentifierScheme: " + oaiIdentifierScheme);
                	oaiConf.setOaiIdentifierScheme(oaiIdentifierScheme);
                }
                String repositoryName = applConf.getString(applVer + "." + "repositoryName", "");
                if (! repositoryName.equals("")) {
                    System.out.println("ApplInfo.oaiConf::repositoryName: " + repositoryName);
                	oaiConf.setRepositoryName(repositoryName);
                }
                String oaiIdentifierDelimiter = applConf.getString(applVer + "." + "oaiIdentifierDelimiter", "");
                if (! oaiIdentifierDelimiter.equals("")) {
                    System.out.println("ApplInfo.oaiConf::oaiIdentifierDelimiter: " + oaiIdentifierDelimiter);
                	oaiConf.setOaiIdentifierDelimiter(oaiIdentifierDelimiter);
                }
                String oaiIdentifierRepositoryIdentifier = applConf.getString(applVer + "." + "oaiIdentifierRepositoryIdentifier", "");
                if (! oaiIdentifierRepositoryIdentifier.equals("")) {
                    System.out.println("ApplInfo.oaiConf::oaiIdentifierRepositoryIdentifier: " + oaiIdentifierRepositoryIdentifier);
                	oaiConf.setOaiIdentifierRepositoryIdentifier(oaiIdentifierRepositoryIdentifier);
                }
	            String oaiIdentifierSampleIdentifier = applConf.getString(applVer + "." + "oaiIdentifierSampleIdentifier", "");
	            if (! oaiIdentifierSampleIdentifier.equals("")) {
	                System.out.println("ApplInfo.oaiConf::oaiIdentifierSampleIdentifier: " + oaiIdentifierSampleIdentifier);
	            	oaiConf.setOaiIdentifierSampleIdentifier(oaiIdentifierSampleIdentifier);
	            }
	            String description = applConf.getString(applVer + "." + "description", "");
                if (! description.equals("")) {
                    System.out.println("ApplInfo.oaiConf::description: " + description);
                	oaiConf.setDescription(description);
                }            	
            }
			
			
			if(!oaiConf.getStorageType().equals(StorageTypes.MYSQL)) {
				if(luceneDir == null) {
					System.out.println("Lucene directory (luceneDir in OAIToolkit.directory.properties)" +
							" isn't set. Please set this directory!");
				} else {
					luceneSearcher = new LuceneSearcher(luceneDir);
				}
			}

			// load metadata formats
			prglog.trace("[PRG] reading metadata formats");
			metadataFormats = MetadataFormatUnmarshaler.load(
					new File(resourceDir, "metadataFormats.xml"), 
					new File(resourceDir, "metadata-format-mapping.xml"));
			
			
			initLogging(configDirName, 
					logDir, OAI_LOG4J_CNF_FILE);
			initDB(configDirName);
			initSets();
			

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void initSets() throws Exception {
		SetsMgr mgr = new SetsMgr();
		List<DataTransferObject> all = mgr.get(new SetDTO());
		for(DataTransferObject item : all) {
			SetDTO set = (SetDTO)item;
			setNamesById.put(set.getSetId(), set.getSetSpec());
			setIdsByName.put(set.getSetSpec(), set.getSetId());
		}
	}

	public static String getLogDir() {
		return logDir;
	}

	public static void setLogDir(String logDir) {
		ApplInfo.logDir = logDir;
	}

	public static String getResourceDir() {
		return resourceDir;
	}

	public static void setResourceDir(String resourceDir) {
		ApplInfo.resourceDir = resourceDir;
	}
	
	public static String getOrgCodeFilter() {
		return orgCodeFilter;
	}

	public static String getOrgCodeFilterXlateOaiIdentifierFrom() {
		return orgCodeFilterXlateOaiIdentifierFrom;
	}

	public static String getOrgCodeFilterXlateOaiIdentifierTo() {
		return orgCodeFilterXlateOaiIdentifierTo;
	}

	
}
