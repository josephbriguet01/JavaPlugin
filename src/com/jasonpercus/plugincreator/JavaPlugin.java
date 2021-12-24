/*
 * Copyright (C) JasonPercus Systems, Inc - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by JasonPercus, December 2021
 */
package com.jasonpercus.plugincreator;



import com.jasonpercus.util.File;
import com.jasonpercus.util.OS;



/**
 * This class represents the main class of the project
 * @author JasonPercus
 * @version 1.0
 */
public final class JavaPlugin extends com.jasonpercus.plugincreator.PluginCreator {

    

//OVERRIDED
    /**
     * Returns the author of the plugin [Required]
     * @return Returns the author of the plugin
     */
    @Override
    public String author() {
        return "JasonPercus";
    }

    /**
     * Return a general description of what the plugin does. This string is displayed to the user in the Stream Deck store [Required]
     * @return Return a general description of what the plugin does. This string is displayed to the user in the Stream Deck store
     */
    @Override
    public String description() {
        return "Allows you to run a Java plugin";
    }

    /**
     * Return the name of the plugin. This string is displayed to the user in the Stream Deck store [Required]
     * @return Return the name of the plugin. This string is displayed to the user in the Stream Deck store
     */
    @Override
    public String name() {
        return "JavaPlugin";
    }

    /**
     * Return the version of the plugin which can only contain digits and periods. This is used for the software update mechanism [Required]
     * @return Return the version of the plugin which can only contain digits and periods. This is used for the software update mechanism
     */
    @Override
    public String version() {
        return "1.0";
    }
    
    /**
     * Returns the name of the folder where the plugin will be stored [Required]
     * @return Returns the name of the folder where the plugin will be stored
     */
    @Override
    public String folderName() {
        return "com.jasonpercus.javaplugin";
    }

    /**
     * Allows you to install the plugin at its final destination
     * @return Returns an input stream pointing to a zip file (in the classpath) containing the plugin that must be deployed. Warning: This zip file must especially not contain the executable
     */
    @Override
    public java.io.InputStream install() {
        return JavaPlugin.class.getResourceAsStream("install/"+folderName()+".sdPlugin.zip");
    }
    
    
    
//MAIN
    /**
     * Corresponds to the plugin startup method
     * @param args Corresponds to the arguments provided by Stream Deck
     */
    public static void main(String[] args) {
        if(args.length == 0 && OS.IS_WINDOWS){
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
             */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Windows".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(Screen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>

            java.net.URL path = PluginCreator.class.getProtectionDomain().getCodeSource().getLocation();
            try {
                java.io.File file = new java.io.File(path.toURI());
                String extension = File.getExtension(file);
                if(extension != null && extension.equals("exe")){
                    //Plugin Init
                    com.jasonpercus.plugincreator.PluginCreator.register(args);
                }
                if(extension != null && extension.equals("jar")){
                    /* Create and display the form */
                    java.awt.EventQueue.invokeLater(() -> {
                        new Screen2().setVisible(true);
                    });
                }
            } catch (java.net.URISyntaxException | java.io.FileNotFoundException ex) {
                /* Create and display the form */
                java.awt.EventQueue.invokeLater(() -> {
                    new Screen2().setVisible(true);
                });
            }
        }else{
            //Plugin Init
            com.jasonpercus.plugincreator.PluginCreator.register(args);
        }
    }
    
    
    
}