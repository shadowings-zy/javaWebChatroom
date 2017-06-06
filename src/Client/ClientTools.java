package Client;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/*
 * 客户端所用工具方法集
 */
public class ClientTools {
	
	/*
	 * 发送图片方法
	 */
	public static String readPic(File file) {
		BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
		String pic = "";
		try {
			image = ImageIO.read(file);
			int rgb;
			int m = image.getHeight();
			int n = image.getWidth();
			pic = m + "," + n + ",";
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					rgb = image.getRGB(i, j);
					pic = pic + i + "," + j + "," + rgb + ",";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pic;
	}
	
	/*
	 * 更改图片方法
	 */
	public static void drawPicture(String picture,String location) {
		try {
			StringTokenizer st = new StringTokenizer(picture, ",");
			int Height = Integer.parseInt(st.nextToken());
			int Width = Integer.parseInt(st.nextToken());
			int x,y,rgb;
			BufferedImage image = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
			
			for (int i = image.getMinX(); i < Width; i++) {
				for (int j = image.getMinY(); j < Height; j++) {
					x = Integer.parseInt(st.nextToken());
					y = Integer.parseInt(st.nextToken());
					rgb = Integer.parseInt(st.nextToken());
					image.setRGB(x, y, rgb);
				}
			}
			
			FileOutputStream fos = new FileOutputStream(location);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
			encoder.encode(image);
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/*
	 *JTextPane插入图片 
	 */
	public static void insertIcon(File file, JTextPane textArea, StyledDocument doc) {
		textArea.setCaretPosition(doc.getLength());// 设置插入位置
		textArea.insertIcon(new ImageIcon(file.getPath())); // 插入图片
		try {
			doc.insertString(doc.getLength(), "\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *在线人员列表 
	 */
	public static int position(String[] strs, String s, int num) {
		for (int i = 0; i < num; i++) {
			if (strs[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}
}