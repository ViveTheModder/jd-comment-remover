package cmd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main 
{
	public static void edit(File src) throws IOException
	{
		boolean startsWithComment=false;
		Scanner sc = new Scanner(src);
		String output="";
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			startsWithComment = line.startsWith("/*");
			if (line.equals("\n") || line.equals("")) //end loop and apply changes if empty line is encountered
			{
				String canonPath = src.getCanonicalPath();
				String delimiter = File.separator;
				if (delimiter.equals("\\")) delimiter+="\\"; //prevent PatternSyntaxException on Windows
				String[] canonPathArray = canonPath.split(delimiter);
				String fileName = canonPathArray[canonPathArray.length-1];
				String folderName = canonPathArray[canonPathArray.length-2];
				System.out.println("Currently overwriting: "+folderName+File.separator+fileName);
				FileWriter writer = new FileWriter(src);
				writer.write(output);
				writer.close(); 
				return;
			}
			if (!startsWithComment) break;
			String line2 = line.replaceAll("/\\*.*\\*/ ", "")+"\n"; //remove comment with regex
			output+=line2;
		}
		sc.close();
	}
	public static void error(Exception exc)
	{
		try 
		{
			File log = new File("errors.log");
			FileWriter writer = new FileWriter(log,true);
			String date = new SimpleDateFormat("dd-MM-yy-hh-mm-ss").format(new Date());
			writer.append(date+": "+exc.getMessage()+"\n");
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public static void traverse(File src)
	{
		if (src.isDirectory())
		{
			String[] list = src.list();
			if (list!=null) //check for subfolders
			{
				for (String name: list)
				{
					File src2 = new File(src,name);
					traverse(src2);
				}
			}
		}
		else if (src.isFile())
		{
			try 
			{
				if (src.getName().endsWith(".java")) edit(src);
			} 
			catch (IOException exc) 
			{
				error(exc);
			}
		}
	}
	public static void main(String[] args) 
	{
		Scanner sc = new Scanner(System.in);
		while (true)
		{
			System.out.println("Enter a valid path containing decompiled .java files:");
			String path = sc.nextLine();
			File src = new File(path);
			if (src.isDirectory())
			{
				sc.close();
				long start = System.currentTimeMillis();
				traverse(src);
				long end = System.currentTimeMillis();
				double time = (end-start)/(double)1000;
				System.out.println("Time: "+time+" s");
				break;
			}
		}
	}
}