package client.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class VoiceSender implements Runnable {
	private String ip;
	private InetAddress ipAddr;
	private int port = 52201;
	private final int MTU = 1500;
	protected boolean running;

	public VoiceSender(String ip, int port) {
		this.ip = ip;
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
			ipAddr = InetAddress.getByName(ip);
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(new byte[MTU], MTU, ipAddr, port);

			// 마이크 입력 연결
			AudioFormat format = new AudioFormat(8000, 16, 1, true, true);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
			line.open();
			line.start();
			int bufSize = (int) format.getSampleRate() * format.getFrameSize();
			byte[] buf = new byte[bufSize];

			while (running) {
				line.read(buf, 0, bufSize);
				int sum = 0;
				while (sum < bufSize) {
					byte[] data = new byte[MTU];
					int i;
					for (i = sum; i < MTU + sum && i < bufSize; i++)
						data[i - sum] = buf[i];
					sum = i;
					packet.setData(data);
					socket.send(packet);
				}
				System.out.println(sum + " / " + bufSize);
			}
			socket.close();
			line.close();
		} catch (LineUnavailableException e) {
			System.err.println("마이크 장치 끊김: " + e);
		} catch (UnknownHostException e1) {
			System.err.println("잘못된 ip주소: " + e1);
		} catch (SocketException e2) {
			System.err.println("연결 끊김: " + ip + ":" + port + e2);
		} catch (IOException e3) {
			System.err.println("전송실패: " + e3);
		}
	}

}
