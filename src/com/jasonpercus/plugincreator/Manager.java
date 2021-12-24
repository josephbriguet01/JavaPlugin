/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, December 2021
 */
package com.jasonpercus.plugincreator;



import com.google.gson.GsonBuilder;
import com.jasonpercus.plugincreator.models.Context;
import com.jasonpercus.plugincreator.models.Payload;
import com.jasonpercus.plugincreator.models.TitleParameters;
import com.jasonpercus.plugincreator.models.events.ApplicationDidLaunch;
import com.jasonpercus.plugincreator.models.events.ApplicationDidTerminate;
import com.jasonpercus.plugincreator.models.events.DeviceDidConnect;
import com.jasonpercus.plugincreator.models.events.DeviceDidDisconnect;
import com.jasonpercus.plugincreator.models.events.DidReceiveGlobalSettings;
import com.jasonpercus.plugincreator.models.events.DidReceiveSettings;
import com.jasonpercus.plugincreator.models.events.Event;
import com.jasonpercus.plugincreator.models.events.KeyDown;
import com.jasonpercus.plugincreator.models.events.KeyUp;
import com.jasonpercus.plugincreator.models.events.PropertyInspectorDidAppear;
import com.jasonpercus.plugincreator.models.events.PropertyInspectorDidDisappear;
import com.jasonpercus.plugincreator.models.events.SendToPlugin;
import com.jasonpercus.plugincreator.models.events.SendToPropertyInspector;
import com.jasonpercus.plugincreator.models.events.SystemDidWakeUp;
import com.jasonpercus.plugincreator.models.events.TitleParametersDidChange;
import com.jasonpercus.plugincreator.models.events.WillAppear;
import com.jasonpercus.plugincreator.models.events.WillDisappear;
import com.jasonpercus.util.File;
import com.jasonpercus.util.LoaderPlugin;
import com.jasonpercus.util.async.Async;



/**
 * This class represents an EventManager for a Stream Deck button
 * @author JasonPercus
 * @version 1.0
 */
@SuppressWarnings("NestedSynchronizedStatement")
class Manager extends EventManager {

    
    
//ATTRIBUT
    /**
     * Corresponds to the list of plugins (files) associated with their respective Manager
     */
    private final java.util.HashMap<String, EventManager[]> LOADED = new java.util.HashMap<>();
    
    
    
//CONSTRUCTOR
    /**
     * Create a Manager
     */
    public Manager() {
    }
    
    

//METHODE PUBLIC
    /**
     * When the EventManager is destroyed. This happens before the app is closed.
     */
    @Override
    public void onDestroy() {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        m.onDestroy();
                    }
                }
            }
        }
    }
    
    
    
//EVENTS
    /**
     * When an event has been received
     * @param event Corresponds to the Stream Deck event
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void event(Event event, GsonBuilder builder) {
        if(!(event instanceof ApplicationDidLaunch) && 
                !(event instanceof ApplicationDidTerminate) && 
                !(event instanceof DeviceDidConnect) && 
                !(event instanceof DeviceDidDisconnect) && 
                !(event instanceof DidReceiveGlobalSettings) && 
                !(event instanceof DidReceiveSettings) && 
                !(event instanceof KeyDown) && 
                !(event instanceof KeyUp) && 
                !(event instanceof PropertyInspectorDidAppear) && 
                !(event instanceof PropertyInspectorDidDisappear) && 
                !(event instanceof SendToPlugin) && 
                !(event instanceof SendToPropertyInspector) && 
                !(event instanceof SystemDidWakeUp) && 
                !(event instanceof TitleParametersDidChange) && 
                !(event instanceof WillAppear) && 
                !(event instanceof WillDisappear)){
            synchronized(LOADED){
                for(java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()){
                    EventManager[] managers = map.getValue();
                    if(managers != null){
                        for(EventManager m : managers){
                            GsonBuilder b = new GsonBuilder();
                            Async.execute(() -> {
                                m.event(event, b);
                            });
                        }
                    }
                }
            }
        }
    }

    /**
     * Action received after calling the getSettings API to retrieve the persistent data stored for the action
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param jsonSettings This json object contains persistently stored data
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void didReceiveSettings(DidReceiveSettings event, Context context, String jsonSettings, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(jsonSettings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.didReceiveSettings(event, context, jsonSettings, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }

    /**
     * Action received after calling the getGlobalSettings API to retrieve the global persistent data
     * @param event Corresponds to the Stream Deck event
     * @param jsonSettings This json object contains persistently stored data
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void didReceiveGlobalSettings(DidReceiveGlobalSettings event, String jsonSettings, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(jsonSettings, Plugin.class);
        String file = p.pluginFile;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.didReceiveGlobalSettings(event, jsonSettings, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }

    /**
     * When the user presses a key, the plugin will receive the keyDown event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void keyDown(KeyDown event, Context context, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.keyDown(event, context, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }
    
    /**
     * When the user releases a key, the plugin will receive the keyUp event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void keyUp(KeyUp event, Context context, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.keyUp(event, context, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }
    
    /**
     * When an instance of an action is displayed on the Stream Deck, for example when the hardware is first plugged in, or when a folder containing that action is entered, the plugin will receive a willAppear event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void willAppear(WillAppear event, Context context, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        loadPlugin(file);
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.willAppear(event, context, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }

    /**
     * When an instance of an action ceases to be displayed on Stream Deck, for example when switching profiles or folders, the plugin will receive a willDisappear event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void willDisappear(WillDisappear event, Context context, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        loadPlugin(file);
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.willDisappear(event, context, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }

    /**
     * When the user changes the title or title parameters, the plugin will receive a titleParametersDidChange event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param title The new title
     * @param parameters A json object describing the new title parameters
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void titleParametersDidChange(TitleParametersDidChange event, Context context, String title, TitleParameters parameters, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.titleParametersDidChange(event, context, title, parameters, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }

    /**
     * When a device is plugged to the computer, the plugin will receive a deviceDidConnect event
     * @param event Corresponds to the Stream Deck event
     * @param device An opaque value identifying the device
     * @param infos A json object containing information about the device
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void deviceDidConnect(DeviceDidConnect event, String device, DeviceDidConnect.DeviceInfo infos, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.deviceDidConnect(event, device, infos, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * When a device is plugged to the computer, the plugin will receive a deviceDidConnect event
     * @param event Corresponds to the Stream Deck event
     * @param device An opaque value identifying the device
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void deviceDidDisconnect(DeviceDidDisconnect event, String device, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.deviceDidDisconnect(event, device, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * When a monitored application is launched, the plugin will be notified and will receive the applicationDidLaunch event
     * @param event Corresponds to the Stream Deck event
     * @param application The identifier of the application that has been launched
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void applicationDidLaunch(ApplicationDidLaunch event, String application, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.applicationDidLaunch(event, application, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * When a monitored application is terminated, the plugin will be notified and will receive the applicationDidTerminate event
     * @param event Corresponds to the Stream Deck event
     * @param application The identifier of the application that has been closed
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void applicationDidTerminate(ApplicationDidTerminate event, String application, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.applicationDidTerminate(event, application, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * When the computer is wake up, the plugin will be notified and will receive the systemDidWakeUp event
     * @param event Corresponds to the Stream Deck event
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void systemDidWakeUp(SystemDidWakeUp event, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.systemDidWakeUp(event, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * Action received when the Property Inspector appears in the Stream Deck software user interface, for example when selecting a new instance
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void propertyInspectorDidAppear(PropertyInspectorDidAppear event, Context context, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.propertyInspectorDidAppear(event, context, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * Action received when the Property Inspector for an instance is removed from the Stream Deck software user interface, for example when selecting a different instance
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void propertyInspectorDidDisappear(PropertyInspectorDidDisappear event, Context context, GsonBuilder builder) {
        synchronized (LOADED) {
            for (java.util.Map.Entry<String, EventManager[]> map : LOADED.entrySet()) {
                EventManager[] managers = map.getValue();
                if (managers != null) {
                    for (EventManager m : managers) {
                        GsonBuilder b = new GsonBuilder();
                        Async.execute(() -> {
                            m.propertyInspectorDidDisappear(event, context, b);
                            m.event(event, b);
                        });
                    }
                }
            }
        }
    }

    /**
     * Action received by the plugin when the Property Inspector uses the sendToPlugin event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param payload Corresponds to the data to send to the plugin
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void sendToPlugin(SendToPlugin event, Context context, Payload payload, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.sendToPlugin(event, context, payload, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }

    /**
     * Action received by the Property Inspector when the plugin uses the sendToPropertyInspector event
     * @param event Corresponds to the Stream Deck event
     * @param context Corresponds to the context (or ID) of the action
     * @param payload Corresponds to the data to send to the PropertyInspector
     * @param builder Allows to deserialize the received json
     */
    @Override
    public void sendToPropertyInspector(SendToPropertyInspector event, Context context, Payload payload, GsonBuilder builder) {
        Plugin p = builder.create().fromJson(event.payload.settings, Plugin.class);
        String file = p.pluginFile;
        String name = p.actionName;
        event.action = name;
        synchronized(LOADED){
            if(file != null && LOADED.containsKey(file)){
                for(EventManager m : LOADED.get(file)){
                    GsonBuilder b = new GsonBuilder();
                    Async.execute(() -> {
                        m.sendToPropertyInspector(event, context, payload, b);
                        m.event(event, b);
                    });
                }
            }
        }
    }
    
    
    
//METHODES PRIVATES
    /**
     * Loads a file representing a plugin
     * @param pluginFile Corresponds to the file path
     */
    private void loadPlugin(String pluginFile){
        java.io.File pf = new java.io.File(pluginFile);
        try {
            if(File.getExtension(pf).equals("pesd") || File.getExtension(pf).equals("jar")){
                boolean contains;
                synchronized(LOADED){
                    contains = LOADED.containsKey(pluginFile);
                }
                if (!contains) {
                    Object[] pluginClass = getObject(pluginFile, EventManager.class);
                    if (pluginClass != null && pluginClass.length > 0) {
                        EventManager[] ems = new EventManager[pluginClass.length];
                        for (int i = 0; i < pluginClass.length; i++) {
                            EventManager em = (EventManager) pluginClass[i];
                            em.setLOGGER(LOGGER);
                            em.setConnectionManager(manager);
                            em.setOPTIONS(OPTIONS);
                            em.setConnection(CONNECTION);
                            ems[i] = em;
                        }
                        synchronized (LOADED) {
                            LOADED.put(pluginFile, ems);
                        }
                    }
                }
            }
        } catch (java.io.FileNotFoundException ex) {}
    }
    
    /**
     * Returns the list of objects contained in the plugin file
     * @param file Corresponds to the file path
     * @param classOrInterfaceSearched Corresponds to the classes sought
     * @return Returns the list of objects contained in the plugin file
     */
    private Object[] getObject(String file, Class... classOrInterfaceSearched){
        return getObject(new java.io.File(file), classOrInterfaceSearched);
    }
    
    /**
     * Returns the list of objects contained in the plugin file
     * @param file Corresponds to the file
     * @param classOrInterfaceSearched Corresponds to the classes sought
     * @return Returns the list of objects contained in the plugin file
     */
    private Object[] getObject(java.io.File file, Class... classOrInterfaceSearched){
        java.util.List<String> list = new java.util.ArrayList<>();
        try{
            java.net.URL u = file.toURL();
            java.net.URLClassLoader loader = new java.net.URLClassLoader(new java.net.URL[]{u});
            java.util.jar.JarFile jar = new java.util.jar.JarFile(file.getAbsolutePath());
            //On récupére le contenu du jar
            java.util.Enumeration enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                String tmp = enumeration.nextElement().toString();
                //On vérifie que le fichier courant est un .class (et pas un fichier d'informations du jar)
                if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0) {
                    tmp = tmp.substring(0, tmp.length() - 6);
                    tmp = tmp.replaceAll("/", ".");
                    Class tmpClass = Class.forName(tmp, true, loader);
                    if (isExtendsOrImplements(classOrInterfaceSearched, tmpClass)) {
                        list.add(tmp);
                    }
                }
            }
            sortActionClass(list);
            Object[] objs = new Object[list.size()];
            for(int i = 0; i < list.size(); i++){
                objs[i] = Class.forName(list.get(i), true, loader).newInstance();
            }
            return objs;
        } catch (java.net.MalformedURLException ex) {
            java.util.logging.Logger.getLogger(LoaderPlugin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (java.io.IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoaderPlugin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Sorts the list of EventManagers by retrieving those which are really valid
     * @param className Corresponds to the list of class names which are potentially good and which must be validated
     */
    private static void sortActionClass(java.util.List<String> className){
        java.util.List<String> toRemove = new java.util.ArrayList<>();
        for(String class1 : className){
            for(String class2 : className){
                if(!class1.equals(class2)){
                    try {
                        Class<?> c1 = Class.forName(class1);
                        Class<?> c2 = Class.forName(class2);
                        if(c1 != c2 && c2.isAssignableFrom(c1)){
                            if(!toRemove.contains(class2)){
                                toRemove.add(class2);
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                        
                    }
                }
            }
        }
        for(String str : toRemove){
            className.remove(str);
        }
        toRemove.clear();
        for(String str : className){
            try {
                Class<?> c = Class.forName(str);
                if(java.lang.reflect.Modifier.isAbstract(c.getModifiers())){
                    if(!toRemove.contains(str)){
                        toRemove.add(str);
                    }
                }
            } catch (ClassNotFoundException ex) {
                
            }
        }
        for(String str : toRemove){
            className.remove(str);
        }
    }
    
    /**
     * Returns whether or not a subclass extends or implements one of the superclasses
     * @param superClass Corresponds to the list of reference super classes
     * @param subClass Corresponds to the subclass to test
     * @return Returns true if the subclass extends or implements
     */
    private boolean isExtendsOrImplements(Class[] superClass, Class subClass){
        for (Class superClas : superClass) {
            if (!((superClas.isAssignableFrom(subClass)) && (!superClas.getName().equals(subClass.getName()))))
                return false;
        }
        return true;
    }
    
    
    
//CLASS
    /**
     * This class represents the settings received in the payloads. In order to retrieve the plugin file path
     * @author JasonPercus
     * @version 1.0
     */
    private class Plugin {
        
        
        
    //ATTRIBUTS
        /**
         * Corresponds to the path of the plugin file
         */
        public String pluginFile;
        
        /**
         * Corresponds to the name of the action associated with the button
         */
        public String actionName;
        
        
        
    }
    
    
    
}