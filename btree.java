  /*Written by AKSHAYA RAMASWAMY for CS6364.004, assignment 5, Date of Submission: November 15, 2017.
  NetID:axr170131 
Implemented a B+  tree in Java where the value of the nodes are read from a text file containing data
 and the tree is built considering the n columns as the key. The program uses random access I/O in java.
*/

package ass5;

// All the necessary packages have been imported.

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;



public class btree {

//The  variables are defined over here.
	
	static RandomAccessFile random;
	
	static FileReader fileread;
	static FileReader fileread1;
	
	static BufferedReader bufferread;
	static BufferedReader bufferread1;
	
	static FileWriter filewrite;
	
	static String left_off="0000000000";
	static String right_off="0000000000";
	static String current_off;
	static int data_off;
	
	static int length;
	static String Level="001";

	static char dir='L';

	static int read = 0;

	static String blk="001";
	static int size_of_block;
	static String ele="001";
	static	String key=null;
		
	
	static int ad_value;
	
	static String arr_off[]=null;
	
/*
 This is the class constructor where the variables have been initialised
 */
	public btree(int i, String val, String data) throws IOException
	{
		
		fileread=new FileReader(new File("offset.txt"));
		fileread1=new FileReader(new File(val));
		
		filewrite=new FileWriter(new File("offset.txt"));
		
	    random=new RandomAccessFile(val, "rw");
		
	    bufferread=new BufferedReader(fileread);
		bufferread1 = new BufferedReader(fileread1);
		
		length=i;
		ad_value=49+i;
		
		current_off=String.valueOf(data.length()+1+String.valueOf(i).length()+2);
		data_off=data.length()+1+String.valueOf(i).length()+2;
	}
	
/*
 The main method is accepting a single argument of type String. The main method gives a 
 call to all the required functions to perform the necessary operations.
 */

	
	@SuppressWarnings({ "unused", "resource" })
	public static void main(String[] args) throws IOException 
	{
			Scanner in=new Scanner(System.in);
			String str=in.nextLine();
			String[] dash=str.split("-");
			String[] space = dash[1].split(" ");
			btree tree;
			if(space[0].equalsIgnoreCase("create"))
			{
				Path path=Paths.get(space[3]);
				Files.deleteIfExists(path);
				tree=new btree(Integer.parseInt(space[1]),space[3],space[2]);
			}
			else
			{
				RandomAccessFile r_a_f=new RandomAccessFile(space[2], "rw");
				String[] key=r_a_f.readLine().split("\t");
				tree=new btree(Integer.parseInt(key[1]),space[2],space[1]);
			}
			
			if(space[0].equalsIgnoreCase("create"))
			{
	    		get_offset(length,space[0],"",space[2]);
	    		RandomAccessFile r_a_f=new RandomAccessFile("offset.txt", "rw");
				size_of_block=(int) (r_a_f.length()/(11+length));
				size_of_block=size_of_block/3;
				if(size_of_block<4)size_of_block=100;
				create(space[0],space[2]);
			}

			boolean t;
			
			if(space[0].equalsIgnoreCase("insert"))
			{
				RandomAccessFile r_a_f=new RandomAccessFile("offset.txt", "rw");
				size_of_block=(int) (r_a_f.length()/(11+length));
				size_of_block=size_of_block/3;
				if(size_of_block<4)size_of_block=100;
				String[] rec=dash[1].split("\"");
				get_offset(length,space[0],rec[1],space[1]);
				String[] split=space[0].split(" ");
				if(!find_records(space[1],split[0],"insert"))create("insert",space[1]);
			}

			if(space[0].equalsIgnoreCase("find"))
			{
				RandomAccessFile r_a_f=new RandomAccessFile("offset.txt", "rw");
				size_of_block=(int) (r_a_f.length()/(11+length));
				size_of_block=size_of_block/3;
				if(size_of_block<4)size_of_block=100;
				t=find_records(space[1],space[3],space[0]);
			}
				
			if(space[0].equalsIgnoreCase("list"))
			{
				RandomAccessFile r_a_f=new RandomAccessFile("offset.txt", "rw");
				size_of_block=(int) (r_a_f.length()/(11+length));
				size_of_block=size_of_block/3;
				if(size_of_block<4)size_of_block=100;
				list_records(space[1],space[3],space[4]);
			}	
		
	}
	
	
/*
 This function is called by the main method to build the index file.
 */
	private static void create(String cmd, String data) throws IOException 
	{
		RandomAccessFile random_offset=new RandomAccessFile("offset.txt", "rw");
		
		if(cmd.equalsIgnoreCase("create"))
		{
			random_offset.seek(0);
			read=0;
		}
		else if(cmd.equalsIgnoreCase("insert"))
		{
			int len=(int) random_offset.length();
			len=len-(11+length+2);
			random_offset.seek(len);
			read=len/(11+length+2-1);
		}
		String str=random_offset.readLine();
		while(str!=null)
		{
			read++;
			arr_off=str.split(",");
			if(random.length()==0)
			{
				right_off=arr_off[0];
				random.writeBytes(data+"\t"+length+"\r\n");
				current_off=hex(Integer.parseInt(current_off));
				random.writeBytes(Level+" "+blk+" "+ele+" "+arr_off[1]+" "+dir+" "+left_off+" "+right_off+" "+current_off+"\r\n");
				current_off=get_next_offset(current_off);
			}
			else
				insert_records(arr_off);
			if(cmd.equalsIgnoreCase("insert")) 
			    System.out.println("The record has been inserted "+arr_off[1]+"at the position "+read);
			str=random_offset.readLine();
			read++;
		}
		random_offset.close();
	}
 
	
	/*
	This function is called by the main method to insert records in the text file.
	 */
	private static void insert_records(String[] arr) throws IOException 
	{
		long pointer=traverse_record(arr[1]);
		random.seek(pointer);
		String str;
		str=random.readLine();
		String[] string_index;
		int block_count=get_blk_cnt(str);
		string_index=str.split(" ");
		String blk=string_index[1];
		random.seek(pointer);
		int offset = 0;
		if(block_count<size_of_block)
		{
			while(str!=null)
			{
				string_index=str.split(" ");
				if(string_index[3].compareTo(arr[1])<0 && string_index[1].equals(blk))
				{
					offset=decimal(string_index[7]);
					str=random.readLine();
				}
				else if(string_index[3].compareTo(arr[1])==0)
				{
					System.out.println(" There  is a duplicate record "+string_index[3]+", present at line "+read);
					return;
				}
				else if(string_index[3].compareTo(arr[1])>0 && string_index[1].equals(blk))
				{
					if(Integer.parseInt(string_index[2])<size_of_block)
					{
						insert_shift(arr,string_index[7],string_index);
						return;
					}
					else
					{
						split(pointer);
						insert_records(arr);
						
					}
				}
				else break;
			}
			random.seek(offset);
			String string_1=random.readLine();
			String[] previous_str = null;
			if(string_1!=null)
			{	
				previous_str=string_1.split(" ");
				if(Integer.parseInt(previous_str[2])<size_of_block)
				{
					int off=(int) random.getFilePointer();
					random.seek(off);
					if(random.readLine()!=null)
					{
						previous_str[2]=String.format("%03d", Integer.parseInt(previous_str[2])+1);
						insert_shift(arr,hex(off),previous_str);
					}
					else
						random.writeBytes(previous_str[0]+" "+previous_str[1]+" "+String.format("%03d",Integer.parseInt(previous_str[2])+1)+" "+arr[1]+" "+"L"+" "+previous_str[5]+" "+arr[0]+" "+hex(off)+"\r\n");
				}
			}
		}
		else
		{
			if(string_index[3].compareTo(arr[1])==0)
			{
				System.out.println("There is a duplicate record  "+string_index[3]+",present at line number "+read);
				return;
			}
			split(pointer);
			insert_records(arr);			
		}
	}
	
	/*
	This function is used for listing the records in a sequential order.
	 */
	
	private static void list_records(String str,String val, String co_str) throws IOException 
	{
		 long pointer=traverse_record(val);
		 int count=decimal(co_str);
	     RandomAccessFile fileread = new RandomAccessFile(str, "rw");
	  
	     
	     random.seek(pointer);
	     String line=random.readLine();
	     String s_arr[]=line.split(" ");
	     
	     while(line!=null)
	     {
	        if(s_arr[3].equals(val))
	        {
	           fileread.seek(decimal(s_arr[6]));
	           System.out.println(decimal(s_arr[6])+"         "+fileread.readLine());
	           for(int i=0;i<count;i++)
	           {
	               line=random.readLine();
	               if(line==null)
	               {
	                  fileread.close();
	                  return;
	               }
	               s_arr=line.split(" ");
	               fileread.seek(decimal(s_arr[6]));
	               System.out.println(decimal(s_arr[6])+"         "+fileread.readLine());
	           }
	           fileread.close();
	           return;
	         }
	   
	         else if(s_arr[3].compareTo(val)>0)
	         {
	            System.out.println("The  key could not be found.\n The remaining keys are\n");
	            fileread.seek(decimal(s_arr[6]));
	            for(int i=0;i<count;i++)
	            {
	               line=random.readLine();
	               if(line==null)
	               {
	                 fileread.close();
	                 return;
	               }
	               s_arr=line.split(" ");
	               fileread.seek(decimal(s_arr[6]));
	               System.out.println(decimal(s_arr[6])+"         "+fileread.readLine());
	            }
	            fileread.close();
	            return;
	         }
	         line=random.readLine();
	         if(line!=null) s_arr=line.split(" ");
	    }
	    fileread.close();
	}
	
	
	/*
	This function is used to pad the record with 0s from the  left side.
	 */
	private static String pad_left(String s) 
	{
		int len=10-s.length();
		for (int i=0; i<len; i++) 
			s = "0" + s;
		return s;
	}
	
	/*
	This function is used to convert decimal to hexadecimal.
	 */
	private static String hex(int k) 
	{
		String str=String.valueOf(Integer.toHexString(k));
		str=pad_left(str);
		return str;
	}
	
	
	/*
	This function  is called by the main method to find a record. 
	 */
	
	
	private static boolean find_records(String str,String key, String cmd) throws IOException 
	{
		RandomAccessFile fileread = new RandomAccessFile(str, "rw");
		
		random.seek(0);
		String line=random.readLine();
		
		String[] line_arr=line.split("\t");
			

		if(key.length()<Integer.parseInt(line_arr[1]))
			key=pad_right(key,line_arr[1]);
		else if(key.length()>Integer.parseInt(line_arr[1]))
			key=key.substring(0, Integer.parseInt(line_arr[1]));

		long pointer=traverse_record(key);
		random.seek(pointer);
		String ss=random.readLine();
		String s[]=null;
		if(ss!=null)s=ss.split(" ");
		String blk=s[1];
		
		while(ss!=null && s[1].equals(blk))
		{
			if(s[3].equals(key))
			{
				fileread.seek(decimal(s[6]));
				if(cmd.equalsIgnoreCase("find")) System.out.println("At "+decimal(s[6])+", record: "+fileread.readLine());
				return true;
			}
			ss=random.readLine();
			if(ss!=null) s=ss.split(" ");
		}
		if(cmd.equalsIgnoreCase("find")) System.out.println("Key Not Found...");
		return false;
	}
	
	
	/*
	This function is used to fetch the offset of the next record.
	 */
	private static String get_next_offset(String current_off) 
	{
		int dec=decimal(current_off);
		current_off=hex(dec+ad_value);
		current_off=pad_left(current_off);
		return current_off;
	}
	
	/*
	This function is used to convert hexadecimal to decimal. 
	 */
	private static int decimal(String off) 
	{
		int value=Integer.parseInt(off,16);
		return value;
	}
	
	

	/*
	 This function is used to split the block based on its middle position.
	 */
	private static void split(long pointer) throws IOException
	{
		int mid_index=size_of_block/2;
		random.seek(pointer);
		String str=random.readLine();
		String[] split;
		while(str!=null)
		{
			split = str.split(" ");
			if(Integer.parseInt(split[2])==mid_index)
			{
				make_middle_root((int) pointer,split[7]);
				return;
			}
				str=random.readLine();
		}
	}
	
	/*
	  This function is used for splitting the block and making the middle element the parent.
	  */
	private static void make_middle_root(int pointer, String mptr) throws IOException 
	{
		String[] leftchild = get_l_child(pointer,mptr);
		String[] rightchild = get_r_child(pointer,mptr);
		
		int fg=0;
		int i;
		int fgg=0;
		
		String[] axr=null;
		String level=null;
		
		random.seek(decimal(mptr));
		
		String mid_element=random.readLine();
		String mid_arr[]=mid_element.split(" ");
		String[] parents=get_parent(mid_arr[0]);
		String leftkey[] = null,rightkey[] = null;
		
		if(leftchild[0]!=null)leftkey= leftchild[0].split(" ");
		
		if(rightchild[0]!=null)rightkey = rightchild[0].split(" ");
	
		//When there is only one  node present.
		if(parents[0]==null)
		{
			mid_element=String.format("%03d",Integer.parseInt(mid_arr[0])+1)+" 001 001 "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value)+" "+hex(decimal(rightkey[7])+ad_value)+" "+hex(pointer)+"\r\n";
		
			random.seek(pointer);
			random.writeBytes(mid_element);
			
			rewrite_child(leftchild,rightchild,pointer,0);
		}
		else
		{
			int blk=-1;

			for( i=0;parents[i]!=null && fg==0;i++)
			{
				String[] pp=parents[i].split(" ");
		
				if(pp[3].compareTo(mid_arr[3])<0){}
				else 
				{
					blk=get_blk_cnt(parents[i]);
					String blocknew = pp[2];
					if(blocknew.equals("001"))
						{
						    if(i!=0)
						    {
						    	pp=parents[i-1].split(" ");
						    	blk=get_blk_cnt(parents[i-1]);
						    	pp[2]=String.format("%03d", Integer.parseInt(pp[2])+1);
						    }
						}
					//If the node isnt full then insert the new key in the same node.
					if(blk<size_of_block)
					{
						String[] ppx=parents[i].split(" ");
						move_below(decimal(ppx[7]));
						random.seek(decimal(ppx[7]));
						String s=pp[0]+" "+pp[1]+" "+pp[2]+" "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value)+" "+hex(decimal(rightkey[7])+ad_value)+" "+ppx[7]+"\r\n";
						random.writeBytes(s);
						
						level=pp[0];
						rewrite_child(leftchild,rightchild,pointer,0);
						change_parents(level,mid_arr[3],0,pp[1]);
						fgg=1;
					}
			
					else
					{
						pp=parents[0].split(" ");
						
						move_below(decimal(get_first_node()));
						split(decimal(pp[7]));
						String[] p=get_parent(hex(decimal(pp[0])-1));
						for(int ix=0;p[ix]!=null;ix++)
						{
							axr=p[ix].split(" ");
							if(axr[3].compareTo(mid_arr[3])<0){}
							else
							{
								move_below(decimal(axr[7]));
								random.seek(decimal(axr[7]));
								String s=axr[0]+" "+axr[1]+" "+axr[2]+" "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value+ad_value)+" "+hex(decimal(rightkey[7])+ad_value+ad_value)+" "+axr[7]+"\r\n";
							
								random.writeBytes(s);
								level=axr[0];
								leftchild = get_l_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
								rightchild = get_r_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
								rewrite_child(leftchild,rightchild,decimal(leftkey[7])+ad_value,1);
								change_parents(level,mid_arr[3],1,axr[1]);						
							
								fgg = 1;
							}
						}
						if(fgg==0)
						{
							move_below(decimal(axr[7])+ad_value);
							random.seek(decimal(axr[7])+ad_value);
							String s=axr[0]+" "+axr[1]+" "+axr[2]+" "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value+ad_value)+" "+hex(decimal(rightkey[7])+ad_value+ad_value)+" "+axr[7]+"\r\n";
							fgg = 1;
							random.writeBytes(s);
							level=axr[0];
							leftchild = get_l_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
							rightchild = get_r_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
							rewrite_child(leftchild,rightchild,decimal(leftkey[7])+ad_value,1);
							change_parents(level,mid_arr[3],1,axr[1]);
						}
						if(fgg==1)
							return;
					}
				}
			}
			if(fg==0)
			{
				
				String[] pp=parents[i-1].split(" ");
				String Block_count=pp[2];
				if(decimal(Block_count)<size_of_block)
				{
					move_below(decimal(pp[7])+ad_value);
					random.seek(decimal(pp[7])+ad_value);
					String s=pp[0]+" "+pp[1]+" "+String.format("%03d",decimal(pp[2])+1)+" "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value)+" "+hex(decimal(rightkey[7])+ad_value)+" "+hex(decimal(pp[7])+ad_value)+"\r\n";
					level=pp[0];
					random.writeBytes(s);
					rewrite_child(leftchild,rightchild,decimal(leftkey[7]),0);
					change_parents(level,mid_arr[3],0,pp[1]);
					fgg=1;
				}
				else
				{
					pp=parents[i-size_of_block].split(" ");
					split(decimal(pp[7]));
					String[] p=get_parent(hex(decimal(pp[0])-1));
					for(int ix=0;p[ix]!=null;ix++)
					{
						axr=p[ix].split(" ");
						if(axr[3].compareTo(mid_arr[3])<0){}
						else
						{
							move_below(decimal(axr[7]));
							random.seek(decimal(axr[7]));
							String s=axr[0]+" "+axr[1]+" "+axr[2]+" "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value+ad_value)+" "+hex(decimal(rightkey[7])+ad_value+ad_value)+" "+axr[7]+"\r\n";
							fgg = 1;
							random.writeBytes(s);
							level=axr[0];
							leftchild = get_l_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
							rightchild = get_r_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
							rewrite_child(leftchild,rightchild,decimal(leftkey[7])+ad_value,1);
							change_parents(level,mid_arr[3],1,axr[1]);
						}
					}
					if(fgg==0)
					{
						move_below(decimal(axr[7])+ad_value);
						random.seek(decimal(axr[7])+ad_value);
						axr[2]=String.format("%03d", decimal(axr[2])+1);
						axr[7]= hex(decimal(axr[7])+ad_value);
						String s=axr[0]+" "+axr[1]+" "+axr[2]+" "+mid_arr[3]+" P "+hex(decimal(leftkey[7])+ad_value+ad_value)+" "+hex(decimal(rightkey[7])+ad_value+ad_value)+" "+axr[7]+"\r\n";
						fgg = 1;
						random.writeBytes(s);
						level=axr[0];
						leftchild = get_l_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
						rightchild = get_r_child(pointer+ad_value+ad_value,hex(decimal(mptr)+ad_value+ad_value));
						rewrite_child(leftchild,rightchild,decimal(leftkey[7])+ad_value,1);
						change_parents(level,mid_arr[3],1,axr[1]);
					}
					if(fgg==1)
						return;
				}
			}
			if(fg==1)return;
		}
		
	}
	

	private static void change_parents(String level,String key,int fgg,String Blo) throws IOException 
	{
		int fg=0;
		random.seek(data_off);
		
		String s=random.readLine();
		String ss[];
		
		ss=s.split(" ");
		while(s!=null)
		{
			if(ss[0].equals(level) && !ss[3].equals(key))
			{
				if(ss[3].compareTo(key)>0  && ss[1].equals(Blo))ss[2]=String.format("%03d", decimal(ss[2])+1);
				
				if(fgg==0)ss[5]=hex(decimal(ss[5])+ad_value);
				else if(fgg==1)ss[5]=hex(decimal(ss[5])+ad_value+ad_value);
				
				if(fgg==0) ss[6]=hex(decimal(ss[6])+ad_value);
				else if(fgg==1)ss[6]=hex(decimal(ss[6])+ad_value+ad_value);
				
				random.seek(decimal(ss[7]));
				random.writeBytes(ss[0]+" "+ss[1]+" "+ss[2]+" "+ss[3]+" "+ss[4]+" "+ss[5]+" "+ss[6]+" "+ss[7]+"\r\n");
				
				fg=1;
			}
			s=random.readLine();
			
			if(s!=null)ss=s.split(" ");
			if(!ss[0].equals(level) && fg==1) break;
		}
	}

	/*
	 This function is used to move the position of all records down by one position.
	 */
	private static void move_below(int decimal) throws IOException 
	{
		
		random.seek(decimal);
		
		String r1=random.readLine();
		String r2=random.readLine();
		int pos=decimal+ad_value;
		
		while(r1!=null || r2!=null)
		{
			random.seek(pos);
			if(r1!=null)
			{
				String [] r11=r1.split(" ");
				r11[7]=hex(decimal(r11[7])+ad_value);
		
				if(r11[4].equals("P")) 
				{
					r11[5]=hex(decimal(r11[5])+ad_value);
					r11[6]=hex(decimal(r11[6])+ad_value);
				}
				
				random.writeBytes(r11[0]+" "+r11[1]+" "+r11[2]+" "+r11[3]+" "+r11[4]+" "+r11[5]+" "+r11[6]+" "+r11[7]+"\r\n");
			}
			
			r1=random.readLine();
			random.seek(pos+ad_value);
			
			if(r2!=null)
			{
				String [] r11=r2.split(" ");
				r11[7]=hex(decimal(r11[7])+ad_value);
			
				if(r11[4].equals("P")) 
				
				{
					r11[5]=hex(decimal(r11[5])+ad_value);
					r11[6]=hex(decimal(r11[6])+ad_value);
				}
				
				random.writeBytes(r11[0]+" "+r11[1]+" "+r11[2]+" "+r11[3]+" "+r11[4]+" "+r11[5]+" "+r11[6]+" "+r11[7]+"\r\n");
			}
			
			r2=random.readLine();
			pos=pos+ad_value+ad_value;
		}
		
	}
	
	/*
	 This function is used to get the right children of a B+ tree.
*/
private static String[] get_r_child(long pointer, String mptr) throws IOException 
{
	random.seek(decimal(mptr));

	String[] rightchild = new String[size_of_block*10];
	
	int k=0;
	
	String str=random.readLine();
	String[] arr=str.split(" ");
	
	int level = Integer.parseInt(arr[0]);
	int blk = Integer.parseInt(arr[1]);
	
	while(Integer.parseInt(arr[1])==blk && Integer.parseInt(arr[0])==level && str!=null)
	{
		rightchild[k++] = str;
		str=random.readLine();
		if(str!=null) arr=str.split(" ");
	}
	
	return rightchild;
}

	/*
	This function is used to traverse through the tree and obtain the offset of the block.
	 */
	private static long traverse_record(String str) throws IOException 
	{
		random.seek(data_off);
	
		String str1=random.readLine();
		String[] index_arr = null;
		
		if(str1!=null) index_arr=str1.split(" ");
		
		int fg=0;
		
		String[] parents = null;
		
		if(!index_arr[0].equals("001"))parents=get_parent(String.valueOf("001"));
		
		int off=data_off,i=0;
		
		while(str1!=null)
		{
			fg=0;
			index_arr=str1.split(" ");
		
			if(index_arr[0].equals("001"))
				return decimal(index_arr[7]);
			else
			{
				for(i=0;parents[i]!=null && fg==0;i++)
				{
					index_arr=parents[i].split(" ");
			
					if(str.compareTo(index_arr[3])>0){}
					else if(str.equals(index_arr[3]))
					{
						off=decimal(index_arr[6]);
						random.seek(off);
						str1=random.readLine();
					
						if(str1!=null)index_arr=str1.split(" ");
							fg=1;
					}
					else 
					{
						off=decimal(index_arr[5]);
						random.seek(off);
						str1=random.readLine();
						
						if(str1!=null)index_arr=str1.split(" ");
						fg=1;
					}
				}
				if(fg==0&&i!=0)
				{
					index_arr=parents[i-1].split(" ");
					off=decimal(index_arr[6]);
					str1=random.readLine();
				}
			}
			if(fg==1)
			{
				random.seek(off);
				str1=random.readLine();
			}
		}
		return off;
	}
	
	/*
	This function is called when the tree needs to be rebalanced after the node becomes full.
	 */
	private static void rewrite_child(String[] leftchild, String[] rightchild,	int pointer, int fg) throws IOException 
	{
		String seek=null;
		String str[] = null;
	
		random.seek(pointer);
	
		String[] str1=random.readLine().split(" ");
		String level=str1[0];
		String blk=str1[1];
		
		random.seek(pointer+ad_value);
		
		for(int i=0;i<leftchild.length && leftchild[i]!=null ;i++)
		{
			str=leftchild[i].split(" ");
		
			if(str[0].equals(level))
			{
				str[1]=String.format("%03d", Integer.parseInt(blk)+1);
			}
			
			if(fg==0)str[7]=hex(decimal(str[7])+ad_value);
			random.writeBytes(str[0]+" "+str[1]+" "+str[2]+" "+str[3]+" "+str[4]+" "+str[5]+" "+str[6]+" "+str[7]+"\r\n");
		}
		
		String s1[];
		
		for(int i=0;i<rightchild.length && rightchild[i]!=null ;i++)
		{
			str=rightchild[i].split(" ");
			str[1]=String.format("%03d", Integer.parseInt(str[1])+1);
			str[2]=String.format("%03d", i+1)	;
		
			if(fg==0)str[7]=hex(decimal(str[7])+ad_value);
			seek=str[7];
			
			random.writeBytes(str[0]+" "+str[1]+" "+str[2]+" "+str[3]+" "+str[4]+" "+str[5]+" "+str[6]+" "+str[7]+"\r\n");
		}
		level=str[0];
		String next=random.readLine();
		
		if(next!=null)str=next.split(" ");
		
		while(next!=null && str[0].equals(level))
		{
			str=next.split(" ");
			str[1]=String.format("%03d", Integer.parseInt(str[1])+1);
		
			random.seek(decimal(str[7]));
			random.writeBytes(str[0]+" "+str[1]+" "+str[2]+" "+str[3]+" "+str[4]+" "+str[5]+" "+str[6]+" "+str[7]+"\r\n");
			
			next=random.readLine();
		}
	}
	
	/*
	This function is used to get the parent nodes.
	 */
	private static String[] get_parent(String lev) throws IOException 
	{
		String[] par=new String[size_of_block*10];
		int p_level=Integer.parseInt(lev)+1;
		
		random.seek(data_off);
		int i=0;
		
		String str=random.readLine();
		
		while(str!=null)
		{
			String[] stringsplit=str.split(" ");
			
			if(decimal(stringsplit[0])==p_level)
				par[i++]=str;
		
			str=random.readLine();
		}
		return par;
	}
	

	
	/*
	 This module is used to get the left children of a B+ tree.
	 */
	private static String[] get_l_child(long pointer, String mptr) throws IOException 
	{
		String[] leftchild = new String[size_of_block*10];
		String str;
		int i=0;
		
		while(pointer<decimal(mptr))
		{
			random.seek(pointer);
			str=random.readLine();
		
			leftchild[i++]=str;
			pointer=pointer+ad_value;
		}
		return leftchild;
	}

	/*
	 This function is used to fetch the total number of elements in a block.
	 
	 */
	private static int get_blk_cnt(String string) throws IOException 
	{
		int cnt = 0,blk;
		String[] str=string.split(" ");
		
		int lev=Integer.parseInt(str[0]);
		blk=Integer.parseInt(str[1]);
		
		random.seek(decimal(str[7]));
		
		while(string!=null && Integer.parseInt(str[1])==blk && Integer.parseInt(str[0])==lev)
		{	
			cnt=Integer.parseInt(str[2]);
			string=random.readLine();
		
			if(string!=null)
				str=string.split(" ");
		}
		return cnt;
	}

	/*
	This function is used to insert new records into the file and shift the following lines by one line.
	 */
	private static void insert_shift(String[] s2, String offset, String[] str_ind) throws IOException 
	{
		int pos=decimal(offset);
	
		random.seek(pos);
		
		String s1=random.readLine();
		
		random.seek(pos);
		random.writeBytes(str_ind[0]+" "+str_ind[1]+" "+String.format("%03d",Integer.parseInt(str_ind[2]))+" "+s2[1]+" "+"L"+" "+str_ind[5]+" "+s2[0]+" "+hex(pos)+"\r\n");
		
		while(s1!=null)
		{
			pos=pos+ad_value;
			String str_sp[] = s1.split(" ");
			if(str_sp[0].equals(str_ind[0]) && str_sp[1].equals(str_ind[1]))
				str_sp[2]=String.format("%03d", Integer.parseInt(str_sp[2])+1);
		
			random.seek(pos);
			s1=random.readLine();
			
			random.seek(pos);
			random.writeBytes(str_sp[0]+" "+str_sp[1]+" "+str_sp[2]+" "+str_sp[3]+" "+str_sp[4]+" "+str_sp[5]+" "+str_sp[6]+" "+hex(decimal(str_sp[7])+ad_value)+"\r\n");
		}
		change_par_off();
	}
	
	/*
	 This function is used to modify the root nodes when the number of elements in a node is greater than the key size. 
   */
	private static void change_par_off() throws IOException 
	{
		random.seek(data_off);
		
		String le_off=null;
		String[] str=random.readLine().split(" ");
		String[] par=get_parent("001");
		
		for(int i=0;par[i]!=null;i=i+2)
		{
			String[] p_arr=par[i].split(" ");
			if(i==0)
				p_arr[5]=get_first_node();
			else
			{
				String[] pp=par[i-1].split(" ");
				p_arr[5]=le_off;	
			}
		
			p_arr[6]=get_right_offset(p_arr[3]);
			le_off=p_arr[6];
			
			random.seek(decimal(p_arr[7]));
			random.writeBytes(p_arr[0]+" "+p_arr[1]+" "+p_arr[2]+" "+p_arr[3]+" "+p_arr[4]+" "+p_arr[5]+" "+p_arr[6]+" "+p_arr[7]+"\r\n");
			
			if(par[i+1]!=null)
			{
				p_arr=par[i+1].split(" ");
				p_arr[5]=le_off;
				p_arr[6]=get_right_offset(p_arr[3]);
				le_off=p_arr[6];
			
				random.seek(decimal(p_arr[7]));
				random.writeBytes(p_arr[0]+" "+p_arr[1]+" "+p_arr[2]+" "+p_arr[3]+" "+p_arr[4]+" "+p_arr[5]+" "+p_arr[6]+" "+p_arr[7]+"\r\n");
			}
		}
	}
	
	
	/*
	 This function is used for obtaining the offset of the first leaf node in the B+ tree.
	 */
	private static String get_first_node() throws IOException 
	{
		random.seek(data_off);
		String str=random.readLine();
		
		while(str!=null)
		{
			String[] arr=str.split(" ");
	
			if(arr[0].equals("001"))
				return arr[7];
			
			str=random.readLine();
		}
		
		return null;
	}

	/*
	 This function is used to obtain the position of the first rightchild of the root
	  */
	private static String get_right_offset(String k) throws IOException 
	{
		random.seek(data_off);
		String str=random.readLine();
		
		while(str!=null)
		{
			String[] arr=str.split(" ");
			
			if(arr[0].equals("001") && arr[3].equals(k))
				return arr[7];
			
			str=random.readLine();
		}
		
		return null;
	}
	
	
	/*
	 This function is used to obtain the offset of each record from the text file and then to insert them into
	 new file.
	 */
	
	private static void get_offset(int length, String cmd, String rec,String data) throws IOException 
	{
		FileReader fileread=new FileReader(new File(data));
		
		RandomAccessFile test=new RandomAccessFile(data, "rw");
		RandomAccessFile random_offset=new RandomAccessFile("offset.txt", "rw");
		
		BufferedWriter bufferwrite=new BufferedWriter(filewrite);
		BufferedReader bufferwrite1=new BufferedReader(fileread);
		
		String key=null;
		char c = 0;
		long offset=0;
		
		if(cmd.equalsIgnoreCase("create"))
		{	
			test.seek(0);
			random_offset.seek(0);
		}
		else if(cmd.equalsIgnoreCase("insert"))
		{
			String new_rec[]=rec.split(" ");
			if(find_records(data,new_rec[0],"insert"))
			{
				System.out.println("Record already exists, not inserted.");
				return;
			}
			else
			{
				int len_test=(int) test.length();
				test.seek(len_test);
				test.writeBytes("\r\n");
				
				int start=(int) test.getFilePointer();
				test.writeBytes(rec);
				test.seek(0);
				
				random_offset.seek(0);
			}
		}
		int i=0;
		String current_off=null;
		StringBuffer sb=new StringBuffer();
		try
		{
			offset=0;
			int l=0;
			while(l<test.length())
			{
				if(i==0)
					offset=test.getFilePointer();
				c=(char)test.read();
				if(i<length)
					sb.append(c);
				i++;l++;
				if(i==length)
				{
					current_off=String.valueOf(Integer.toHexString((int) offset));
					current_off=pad_left(current_off);
					
					key=sb.toString(); 
					
					random_offset.writeBytes(current_off+","+key+"\r\n");
		
					sb.delete(0, sb.length());
					
					offset=0;
				}
				if(c=='\n')i=0;
			}
		}	
		catch( IOException e)
		{
			System.out.println("IOException");
		}
		random_offset.close();
		test.close();
	}
	

	
	/*
	This function is used to pad the record with 0s from the  right side.
	 */
	private static String pad_right(String rec, String larr) 
	{
	for (int i=rec.length(); i<Integer.parseInt(larr); i++) 
			rec = rec + " ";
		return rec;
	}
}


/*References-
 * http://www.geeksforgeeks.org/b-tree-set-1-introduction-2/http://www.geeksforgeeks.org/b-tree-set-1-introduction-2/
 *https://en.wikibooks.org/wiki/Algorithm_Implementation/Trees/B%2B_tree
 * https://github.com/vineetdhanawat/btree-indexing/blob/master/btree.cpp
 * https://github.com/parasiempre/B-Plus-Tree/blob/master/DB_Cretate_Index.java
 * http://people.cs.vt.edu/shaffer/Book/JAVA/progs/BPTree/BPTree.java
 * https://www.openhub.net/p/9196
 * */
 