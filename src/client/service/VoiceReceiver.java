package client.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class VoiceReceiver implements Runnable {
	private int port;
	protected boolean running;

	public VoiceReceiver(int port) {
		this.port = port;
	}

	public void stop() {
		running = false;
	}

	@Override
	public void run() {
		running = true;
		try {
			// 소켓, 패킷 생성
			DatagramSocket socket = new DatagramSocket(port);
			DatagramPacket packet = new DatagramPacket(new byte[1500], 1500);

			// 마이크 출력 연결
			AudioFormat format = new AudioFormat(8000, 16, 1, true, true);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open();
			line.start();
			int bufSize = (int) format.getSampleRate() * format.getFrameSize();

			while (running) {
				int sum = 0;
				while (sum < bufSize) {
					socket.receive(packet);
					sum += line.write(packet.getData(), 0, packet.getLength());
					System.out.print(".");
				}
				System.out.println();
			}
			line.drain();
			line.close();
			socket.close();
		} catch (LineUnavailableException e) {
			System.err.println("마이크 장치 끊김: " + e);
		} catch (SocketException e1) {
			System.err.println("연결 끊김: " + port + e1);
		} catch (IOException e2) {
			System.err.println("수신실패: " + e2);
		}
	}

}
