import java.io.*;

public class string_example{

	public static void main(String[] args){
		String line = "url=/#q=";
		String linearr[] = line.split("=",2);
		System.out.println(linearr[0]);
		System.out.println(linearr[1]);
		
	}
}
