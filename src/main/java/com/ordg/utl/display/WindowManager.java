package com.ordg.utl.display;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class WindowManager {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private Map<String, Scene> scenes = new HashMap<String, Scene>();
    private Scene currentScene;

    public WindowManager(JFrame frame) {
        this.frame = frame;
        this.tabbedPane = new JTabbedPane();
        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    public void addScene(String name, JPanel panel) {
        Scene scene = new Scene(panel);
        String sceneName = name;
        int sequence = 1;
        while (scenes.containsKey(sceneName)) {
            sceneName = name + " " + sequence;
            sequence++;
        }
        scenes.put(sceneName, scene);
        currentScene = scene;
        tabbedPane.add(sceneName, panel);
        int index = tabbedPane.indexOfComponent(panel);
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
       tabPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(sceneName);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        tabPanel.add(titleLabel);
       
         if (!name.equals("Main Dashboard"))
        {
        JButton closeButton = new TabButton(tabbedPane);
        tabPanel.add(closeButton);
        }
        
        tabbedPane.setTabComponentAt(index, tabPanel);
        switchScene(sceneName);
    }

    public List<String> getSceneNames() {
        List<String> sceneNames = new ArrayList<String>();
        for (String name : scenes.keySet()) {
            sceneNames.add(name);
        }
        return sceneNames;
    }

    public void switchScene(String name) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            currentScene = scene;
            JPanel panel = currentScene.getView();
            tabbedPane.setSelectedComponent(panel);
        }
    }

    public Scene getCurrentScene() {
        return currentScene;
    }
}

class Scene {
    private JPanel view;

    public Scene(JPanel view) {
        this.view = view;
    }

    public JPanel getView() {
        return view;
    }

    public void setView(JPanel view) {
        this.view = view;
    }
}
/*public class WindowManager {
    private JFrame frame;
    private JScrollPane scrollPane =  new JScrollPane();
    private Map<String, Scene> scenes = new HashMap<String, Scene>();;
    private Scene currentScene;

    public WindowManager(JFrame frame) {
        this.frame = frame;
        frame.add(scrollPane);
    }

    public void addScene(String name, JPanel panel) {
    Scene scene = new Scene(panel);
    String sceneName = name;
    int sequence = 1;
    while (scenes.containsKey(sceneName)) {
        sceneName = name + " " + sequence;
        sequence++;
    }
    scenes.put(sceneName, scene);
    currentScene = scene;
    scrollPane.setViewportView(panel);
}

    
    public List<String> getSceneNames() {
    List<String> sceneNames = new ArrayList<String>();
    for (String name : scenes.keySet()) {
        sceneNames.add(name);
    }
    return sceneNames;
}



  public void switchScene(String name) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            currentScene = scene;
            JPanel panel = currentScene.getView();
            scrollPane.setViewportView(panel);
        }
    }


    public Scene getCurrentScene() {
        return currentScene;
    }
    
    public void closeScene(String name) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            JPanel panel = scene.getView();
            if (panel != null) {
                if (currentScene == scene) {
                    currentScene = null;
                    scrollPane.setViewportView(null);
                }
                panel.removeAll();
                scenes.remove(name);
            }
        }
    }
    
    
    
}

class Scene {
    private JPanel view;

    public Scene(JPanel view) {
        this.view = view;
    }

    public JPanel getView() {
        return view;
    }

    public void setView(JPanel view) {
        this.view = view;
    }
}
*/