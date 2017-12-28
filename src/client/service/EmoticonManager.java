package client.service;

import java.util.Vector;

import javax.swing.ImageIcon;

public class EmoticonManager {
	private static EmoticonManager instance;
	private Vector<String> emotPaths = new Vector<>();

	private EmoticonManager() {
		emotPaths.add("img/1.png");
		emotPaths.add("img/2.png");
		emotPaths.add("img/3.png");
		emotPaths.add("img/4.png");
		emotPaths.add("img/5.png");
		emotPaths.add("img/6.png");
		emotPaths.add("img/7.png");
		emotPaths.add("img/8.png");
	}

	public static EmoticonManager getInstance() {
		if (instance == null)
			instance = new EmoticonManager();
		return instance;
	}

	public ImageIcon getEmoticon(int index) {
		String path = emotPaths.get(index);
		ImageIcon icon = (path != null) ? new ImageIcon(path) : new ImageIcon("");
		return icon;
	}

	public int getSize() {
		return emotPaths.size();
	}

}
