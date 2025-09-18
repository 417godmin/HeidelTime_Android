/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.CompositeResourceFactory;
import org.apache.uima.ResourceFactory;
import org.apache.uima.ResourceSpecifierFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UIMARuntimeException;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.analysis_component.AnalysisComponent_ImplBase;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.CollectionProcessingManager;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.internal.util.I18nUtil;
import org.apache.uima.internal.util.XMLUtils;
import org.apache.uima.resource.ConfigurationManager;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.util.SimpleResourceFactory;
import org.apache.uima.util.UimaTimer;
import org.apache.uima.util.XMLParser;
import org.apache.uima.util.impl.Constants;
import org.apache.uima.util.impl.DummyLogger;
import org.apache.uima.util.impl.Logger_common_impl;
import org.apache.uima.util.impl.XMLParser_impl;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the main interface point to the UIMA reference implemention. Applications cannot use this
 * class directly. Use the static methods on {@link UIMAFramework} instead.
 * <p>
 * This class is a singleton which will be accessed by many threads simulatenously. It must be
 * threadsafe.
 * 
 * 
 */
public class UIMAFramework_impl extends UIMAFramework {

  public static final boolean debug = false;

  /**
   * resource bundle for log messages
   */
  private static final String LOG_RESOURCE_BUNDLE = "org.apache.uima.impl.log_messages";

  private static final String LOGGER_CLASS_SYSTEM_PROPERTY = "org.apache.uima.logger.class";
  /**
   * current class
   */
  private static final Class<UIMAFramework_impl> CLASS_NAME = UIMAFramework_impl.class;

  /**
   * The <code>ResourceFactory</code> used by the UIMA reference implementation.
   */
  private CompositeResourceFactory mResourceFactory;

  /**
   * The <code>ResourceSpecifierFactory</code> used by the UIMA reference implementation.
   */
  private ResourceSpecifierFactory mResourceSpecifierFactory;

  /**
   * The <code>XMLParser</code> used by the UIMA reference implementation.
   */
  private XMLParser mXMLParser;

  /**
   * The class of the <code>Logger</code> used by the UIMA reference implementation.
   */
  private Class mLoggerClass;

  /**
   * The default <code>Logger</code> instance used by the UIMA reference implementation.
   */
  private Logger mDefaultLogger;

  /**
   * CollectionProcessingManager implementation class, an instance of which will be returned by
   * {@link #newCollectionProcessingManager()}.
   */
  private String mCpmImplClassName;

  /**
   * ResourceManager implementation class, an instance of which will be returned by
   * {@link #newDefaultResourceManager()}.
   */
  private String mResourceManagerImplClassName;

  /**
   * ResourceManagerPearWrapper implementation class, an instance of which will be returned by
   * {@link #newDefaultResourceManagerPearWrapper()}.
   */
  private String mResourceManagerPearWrapperImplClassName;

  /**
   * ConfigurationManager implementation class, an instance of which will be returned by
   * {@link #newConfigurationManager()}.
   */
  private String mConfigurationManagerImplClassName;

  /**
   * The class of the <code>UimaContext</code> used by the UIMA reference implementation.
   */
  private String mUimaContextImplClassName;

  /**
   * The class of the <code>CollectionProcessingEngine</code> used by the UIMA reference
   * implementation.
   */
  private String mCpeClassName;

  /**
   * The class of the <code>Timer</code> used by the UIMA reference implementation.
   */
  private String mTimerClassName;

  /**
   * Default performance tuning properties.
   */
  private Properties mDefaultPerformanceTuningProperties;

  /**
   * HashMap includes all log wrapper classes
   */
  private ConcurrentHashMap<String, Logger> mLoggers;

  /**
   * Creates a new UIMAFramework_impl.
   */
  public UIMAFramework_impl() {
    // 提前初始化，避免并发 NPE
    mLoggers = new ConcurrentHashMap<>(200, 1.0f);
    mLoggerClass = DummyLogger.class;
    mDefaultLogger = DummyLogger.getInstance();
  }

  /**
   * @see UIMAFramework#_initialize()
   */
  @Override
  protected void _initialize() throws Exception {
    mResourceFactory = new CompositeResourceFactory_impl();
    mXMLParser = new XMLParser_impl();
    mResourceSpecifierFactory = new ResourceSpecifierFactory_impl();

    // 手动指定默认实现，替代 factoryConfig.xml
    mConfigurationManagerImplClassName = "org.apache.uima.resource.impl.ConfigurationManager_impl";
    mResourceManagerImplClassName = "org.apache.uima.resource.impl.ResourceManager_impl";
    mResourceManagerPearWrapperImplClassName = "org.apache.uima.resource.impl.ResourceManagerPearWrapper_impl";
    mUimaContextImplClassName = "org.apache.uima.impl.UimaContext_impl";
    mCpmImplClassName = "org.apache.uima.collection.impl.cpm.Cpm_impl";
    mCpeClassName = "org.apache.uima.collection.impl.cpe.CPMEngine";
    mTimerClassName = "org.apache.uima.util.impl.JavaTimer";

    // 读取性能参数
    mDefaultPerformanceTuningProperties = new Properties();
    try (InputStream is = UIMAFramework_impl.class.getResourceAsStream("performanceTuning.properties")) {
      if (is != null) {
        mDefaultPerformanceTuningProperties.load(is);
      }
    }

    mLoggers = new ConcurrentHashMap<>(200, 1.0f);
  }

  /**
   * @see UIMAFramework#_getMajorVersion()
   */
  @Override
  public short _getMajorVersion() {
    return 3; // 例如UIMA 3.x
  }

  @Override
  public short _getMinorVersion() {
    return 5; // 例如UIMA 3.5.x
  }

  @Override
  public short _getBuildRevision() {
    return 0; // 例如初始构建
  }

  /**
   * Retrieves a reference to the <code>ResourceFactory</code> used by the UIMA reference
   * implementation.
   * 
   * @return the <code>ResourceFactory</code> to be used by the application
   */
  @Override
  protected CompositeResourceFactory _getResourceFactory() {
    return mResourceFactory;
  }

  /**
   * To be implemented by subclasses; this should return a reference to the
   * <code>ResourceSpecifierFactory</code> used by this implementation.
   * 
   * @return the <code>ResourceSpecifierFactory</code> to be used by the application.
   */
  @Override
  protected ResourceSpecifierFactory _getResourceSpecifierFactory() {
    return mResourceSpecifierFactory;
  }

  /**
   * To be implemented by subclasses; this should return a reference to the UIMA {@link XMLParser}
   * used by this implementation.
   * 
   * @return the <code>XMLParser</code> to be used by the application.
   */
  @Override
  protected XMLParser _getXMLParser() {
    return mXMLParser;
  }

  /**
   * @see UIMAFramework#newCollectionProcessingManager()
   */
  @Override
  protected CollectionProcessingManager _newCollectionProcessingManager(
          ResourceManager aResourceManager) {
    try {
      Class cpmClass = Class.forName(mCpmImplClassName);
      Constructor constructor = cpmClass.getConstructor(new Class[] { ResourceManager.class });
      return (CollectionProcessingManager) constructor
              .newInstance(new Object[] { aResourceManager });

    } catch (InvocationTargetException e) {
      throw new UIMARuntimeException(e.getTargetException());
    } catch (Exception e) {
      throw new UIMARuntimeException(e);
    }
  }

  /**
   * To be implemented by subclasses; this should return a reference to the UIMA {@link Logger} used
   * by this implementation.
   * 
   * @return the <code>Logger</code> to be used by the application.
   */
  @Override
  protected Logger _getLogger() {
    return mDefaultLogger;
  }

  /**
   * To be implemented by subclasses; this should return a reference to the UIMA {@link Logger} of
   * the specified source class.
   * 
   * @return the <code>Logger</code> of the specified source class
   */
  @Override
  protected Logger _getLogger(Class component) {
    // Android 初始化安全防护
    if (mLoggerClass == null) {
      try {
        // 如果 factoryConfig.xml 还没解析到 <logger>，就先用默认的 Android logger
        mLoggerClass = org.apache.uima.util.impl.Slf4jLogger_impl.class; // 或你移植的实现类
        // 创建默认 logger
        mDefaultLogger = (Logger) mLoggerClass
                .getMethod("getInstance", Class.class)
                .invoke(null, UIMAFramework_impl.class);
      } catch (Exception e) {
        throw new UIMARuntimeException(e);
      }
    }
    // search for the source class logger in the HashMap
    Logger o = mLoggers.get(component.getName());

    if (o == null) // source class logger not available
    {
      synchronized (this) {
        o = mLoggers.get(component.getName());

        if (o == null) { // source class logger not available

          // create new Logger for the source class
          // set method argument type
          Class[] argumentTypes = { Class.class };
          // set argument value
          Object[] arguments = { component };
          try {
            // get static method getInstance(Class component)
            Method instanceMethod = mLoggerClass.getMethod("getInstance", argumentTypes);
            // invoke getInstance(Class component) method and retrieve logger object
            o = (Logger) instanceMethod.invoke(null, arguments);
            if (AnalysisComponent_ImplBase.class.isAssignableFrom(component) ||
            // Watch out: next Annotat_ImplBase class exists in 2 packages, this one is old one,
            // needed for backwards compat.
                    org.apache.uima.analysis_engine.annotator.Annotator_ImplBase.class
                            .isAssignableFrom(component)) {
              ((Logger_common_impl) o).setAnnotatorLogger(true);
            }
          } catch (NoSuchMethodException e) {
            throw new UIMARuntimeException(e);
          } catch (InvocationTargetException e) {
            throw new UIMARuntimeException(e);
          } catch (IllegalAccessException e) {
            throw new UIMARuntimeException(e);
          }

          // put created logger to the HashMap
          mLoggers.put(component.getName(), o);
        }
      }
    }

    return o;
  }

  /**
   * @see UIMAFramework#_newLogger()
   */
  @Override
  protected Logger _newLogger() {
    try {
      // get static method getInstance()
      Method instanceMethod = mLoggerClass.getMethod("getInstance", Constants.EMPTY_CLASS_ARRAY);
      // invoke getInstance() method and retrieve default logger object
      return (Logger) instanceMethod.invoke(null, (Object[]) Constants.EMPTY_CLASS_ARRAY);
    } catch (NoSuchMethodException e) {
      throw new UIMARuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new UIMARuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new UIMARuntimeException(e);
    }
  }

  /**
   * To be implemented by subclasses; this should return a new instance of the default
   * {@link ResourceManager} used by this implementation.
   * 
   * @return a new <code>ResourceManager</code> to be used by the application.
   */
  @Override
  protected ResourceManager _newDefaultResourceManager() {
    try {
      return (ResourceManager) Class.forName(mResourceManagerImplClassName).newInstance();
    } catch (InstantiationException e) {
      throw new UIMARuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new UIMARuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new UIMARuntimeException(e);
    }
  }

  /**
   * To be implemented by subclasses; this should return a new instance of the default
   * {@link ResourceManager} used by this implementation.
   * 
   * @return a new <code>ResourceManager</code> to be used by the application.
   */
  @Override
  protected ResourceManager _newDefaultResourceManagerPearWrapper() {
    try {
      return (ResourceManager) Class.forName(mResourceManagerPearWrapperImplClassName)
              .newInstance();
    } catch (InstantiationException e) {
      throw new UIMARuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new UIMARuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new UIMARuntimeException(e);
    }
  }

  /**
   * To be implemented by subclasses; this should return a new instance of the
   * {@link ConfigurationManager} used by this implementation.
   * 
   * @return a new <code>ConfigurationManager</code> to be used by the application.
   */
  @Override
  protected ConfigurationManager _newConfigurationManager() {
    try {
      return (ConfigurationManager) Class.forName(mConfigurationManagerImplClassName).newInstance();
    } catch (InstantiationException e) {
      throw new UIMARuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new UIMARuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new UIMARuntimeException(e);
    }
  }

  /**
   * @see UIMAFramework#_newUimaContext()
   */
  @Override
  protected UimaContextAdmin _newUimaContext() {
    try {
      // 先拿到 ResourceManager 和 ConfigurationManager
      ResourceManager rm = UIMAFramework.newContextResourceManager.get();
      ConfigurationManager cm = UIMAFramework.newContextConfigManager.get();

      // 如果外部没传就新建默认的
      if (rm == null) {
        rm = _newDefaultResourceManager();
      }
      if (cm == null) {
        cm = _newConfigurationManager(); // 建议这里直接 new ConfigurationManager_impl()
      }

      // 关键步骤：放到 ThreadLocal
      UIMAFramework.newContextResourceManager.set(rm);
      UIMAFramework.newContextConfigManager.set(cm);

      // 再去构造 RootUimaContext_impl（无参构造）
      return (UimaContextAdmin) Class.forName(mUimaContextImplClassName).newInstance();
    }
    catch (Exception e) {
      throw new UIMARuntimeException(e);
    }
  }

  /**
   * @see UIMAFramework#_newTimer()
   */
  @Override
  protected UimaTimer _newTimer() {
    try {
      return (UimaTimer) Class.forName(mTimerClassName).newInstance();
    } catch (InstantiationException e) {
      throw new UIMARuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new UIMARuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new UIMARuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.uima.UIMAFramework#_produceCollectionProcessingEngine(org.apache.uima.collection.
   * metadata.cpeDescription, java.util.Map)
   */
  @Override
  protected CollectionProcessingEngine _produceCollectionProcessingEngine(
          CpeDescription aCpeDescription, Map<String, Object> aAdditionalParams)
          throws ResourceInitializationException {
    try {
      CollectionProcessingEngine cpe = (CollectionProcessingEngine) Class.forName(mCpeClassName)
              .newInstance();
      cpe.initialize(aCpeDescription, aAdditionalParams);
      return cpe;
    } catch (InstantiationException e) {
      throw new UIMARuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new UIMARuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new UIMARuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.uima.UIMAFramework#_getDefaultPerformanceTuningProperties()
   */
  @Override
  protected Properties _getDefaultPerformanceTuningProperties() {
    return (Properties) mDefaultPerformanceTuningProperties.clone();
  }

  /**
   * Parses the factoryConfig.xml file and sets up the ResourceFactory, ResourceSpecifierFactory,
   * and XMLParser.
   * 
   * @throws ParserConfigurationException
   *           if the XML parser could not be configured
   * @throws SAXException
   *           if factoryConfig.xml could not be parsed
   * @throws ClassNotFoundException
   *           -
   * @throws IllegalAccessException
   *           -
   * @throws InstantiationException
   *           -
   * @throws IOException
   *           -
   */
  private void parseFactoryConfig() {
    // Android 环境不支持 SAXParserFactory.setXIncludeAware
    // 对 HeidelTime 来说 factoryConfig 仅影响 logger/data path，可直接跳过
  }

  /**
   * SAX Handler for parsing factory config file.
   * 
   * 
   */
  class FactoryConfigParseHandler extends DefaultHandler {
    static final int CONTEXT_NONE = 0;

    static final int CONTEXT_FACTORY_CONFIG = 1;

    static final int CONTEXT_RESOURCE_SPECIFIER = 2;

    static final int CONTEXT_RESOURCE = 3;

    static final int CONTEXT_SIMPLE_FACTORY = 4;

    private int context;

    private SimpleResourceFactory simpleFactory;

    private String simpleFactorySpecifierType;

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
      context = CONTEXT_NONE;
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String,
     *      String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
      if ("logger".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<logger>" }));
        }
        try {
          // get logger class
          // check first if a system property for the logger class was specified
          String loggerClass = System.getProperty(LOGGER_CLASS_SYSTEM_PROPERTY);
          if (loggerClass == null) {
            // no system property was specified, use factoryConfig.xml setting
            loggerClass = attributes.getValue("class");
          }
          mLoggerClass = Class.forName(loggerClass);
          // get static method getInstance()
          Method instanceMethod = mLoggerClass.getMethod("getInstance",
                  Constants.EMPTY_CLASS_ARRAY);
          // invoke getInstance() method and retrieve default logger object
          mDefaultLogger = (Logger) instanceMethod.invoke(null,
                  (Object[]) Constants.EMPTY_CLASS_ARRAY);
        } catch (Exception e) {
          throw new SAXException(e);
        }
      } else if ("cpm".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context", new Object[] { "<cpm>" }));
        }
        mCpmImplClassName = attributes.getValue("class");
      } else if ("resourceManager".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<resourceManager>" }));
        }
        mResourceManagerImplClassName = attributes.getValue("class");
      } else if ("resourceManagerPearWrapper".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<resourceManagerPearWrapper>" }));
        }
        mResourceManagerPearWrapperImplClassName = attributes.getValue("class");
      } else if ("configurationManager".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<configurationManager>" }));
        }
        mConfigurationManagerImplClassName = attributes.getValue("class");
      } else if ("uimaContext".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<uimaContext>" }));
        }
        mUimaContextImplClassName = attributes.getValue("class");
      } else if ("cpe".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context", new Object[] { "<cpe>" }));
        }
        mCpeClassName = attributes.getValue("class");
      } else if ("timer".equals(qName)) {
        if (context != CONTEXT_FACTORY_CONFIG) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<timer>" }));
        }
        mTimerClassName = attributes.getValue("class");
      } else if ("resourceSpecifier".equals(qName)) {
        if (mLoggerClass == null) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "expected_x_but_found_y",
                  new Object[] { "<logger>", "<resourceSpecifier>" }));
        }
        context = CONTEXT_RESOURCE_SPECIFIER;
      } else if ("resource".equals(qName)) {
        if (mLoggerClass == null) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "expected_x_but_found_y",
                  new Object[] { "<logger>", "<resource>" }));
        }
        context = CONTEXT_RESOURCE;
      } else if ("simpleFactory".equals(qName)) {
        if (context != CONTEXT_RESOURCE) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<simpleFactory>" }));
        }
        simpleFactory = new SimpleResourceFactory();
        simpleFactorySpecifierType = attributes.getValue("specifier");
        context = CONTEXT_SIMPLE_FACTORY;
      } else if ("binding".equals(qName)) {
        if (context == CONTEXT_RESOURCE_SPECIFIER) {
          try {
            mXMLParser.addMapping(attributes.getValue("element"), attributes.getValue("class"));
            mResourceSpecifierFactory.addMapping(attributes.getValue("interface"),
                    attributes.getValue("class"));
          } catch (ClassNotFoundException e) {
            // not an error
            if (debug) {
              UIMAFramework.getLogger(CLASS_NAME).logrb(Level.INFO, CLASS_NAME.getName(),
                      "startElement", LOG_RESOURCE_BUNDLE,
                      "UIMA_class_in_framework_config_not_found__INFO", e.getLocalizedMessage());
            }
          }
        } else if (context == CONTEXT_SIMPLE_FACTORY) {
          try {
            simpleFactory.addMapping(attributes.getValue("specifier"),
                    attributes.getValue("resource"));
          } catch (ClassNotFoundException e) {
            // not an error
            if (debug) {
              UIMAFramework.getLogger(CLASS_NAME).logrb(Level.INFO, CLASS_NAME.getName(),
                      "startElement", LOG_RESOURCE_BUNDLE,
                      "UIMA_class_in_framework_config_not_found__INFO", e.getLocalizedMessage());
            }
          }
        } else {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<binding>" }));
        }
      } else if ("customFactory".equals(qName)) {
        if (context != CONTEXT_RESOURCE) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<customFactory>" }));
        }
        try {
          Class specifierClass = Class.forName(attributes.getValue("specifier"));
          Class factoryClass = Class.forName(attributes.getValue("factoryClass"));
          ResourceFactory factory = (ResourceFactory) factoryClass.newInstance();
          mResourceFactory.registerFactory(specifierClass, factory);
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          // not an error
          if (debug) {
            UIMAFramework.getLogger(CLASS_NAME).logrb(Level.INFO, CLASS_NAME.getName(),
                    "startElement", LOG_RESOURCE_BUNDLE,
                    "UIMA_class_in_framework_config_not_found__INFO", e.getLocalizedMessage());
          }
        }
      } else if ("factoryConfig".equals(qName)) {
        if (context != CONTEXT_NONE) {
          throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                  Locale.getDefault(), "element_unexpected_in_context",
                  new Object[] { "<factoryConfig>" }));
        }
        context = CONTEXT_FACTORY_CONFIG;
      } else {
        throw new SAXException(I18nUtil.localizeMessage(UIMAException.STANDARD_MESSAGE_CATALOG,
                Locale.getDefault(), "sax_unknown_element", new Object[] { qName }));
      }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String,
     *      String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("factoryConfig".equals(qName)) {
        context = CONTEXT_NONE;
      } else if ("resourceSpecifier".equals(qName) || "resource".equals(qName)) {
        context = CONTEXT_FACTORY_CONFIG;
      } else if ("simpleFactory".equals(qName)) {
        try {
          Class specifierClass = Class.forName(simpleFactorySpecifierType);
          mResourceFactory.registerFactory(specifierClass, simpleFactory);
        } catch (ClassNotFoundException e) {
          UIMAFramework.getLogger(CLASS_NAME).logrb(Level.INFO, CLASS_NAME.getName(), "endElement",
                  LOG_RESOURCE_BUNDLE, "UIMA_class_in_framework_config_not_found__INFO",
                  e.getLocalizedMessage());
        }
        context = CONTEXT_RESOURCE;
      }
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {
      if (_getLogger() != null) {
        UIMAFramework.getLogger(CLASS_NAME).logrb(Level.WARNING, CLASS_NAME.getName(), "warning",
                LOG_RESOURCE_BUNDLE, "UIMA_factory_config_parse__WARNING", e.getLocalizedMessage());
      }
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
      throw new UIMARuntimeException(e);
    }

  }

}
