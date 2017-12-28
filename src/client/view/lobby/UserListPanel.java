package client.view.lobby;

import java.awt.Dimension;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import client.service.RoomManager;
import client.view.Observer;

public class UserListPanel extends JScrollPane implements Observer, ListSelectionListener {
	private static final long serialVersionUID = 5694576677020621386L;

	private DefaultListModel<String> users = new DefaultListModel<>();
	JList<String> userListView = new JList<>(users);
	private RoomManager manager = RoomManager.getInstance();

	public UserListPanel(int width, int height) {
		setPreferredSize(new Dimension(width, height));

		// User List View
		userListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userListView.addListSelectionListener(this);
		setViewportView(userListView);

		manager.allow(this);
	}

	@Override
	public void update() {
		users.clear();
		for (int i = 0; i < manager.getNumOfUsers(); i++)
			users.addElement(manager.getUser(i));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		String userId = userListView.getSelectedValue();
		JTextField tf = ((LobbyPanel) getParent()).getChatTextField();
		tf.setText("[" + userId + "] ");
		tf.requestFocus();
	}

}
