import java.io.*;
import java.util.Map;
import java.util.List;
import java.nio.file.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Abhishek Singh Panesar
 * @category  BitMask based Compression and Decompression Algorithm for a file @version 1.0
 */
public class SIM {

	// File names of different files.
	protected static HashMap<String,String> dictionary;
	private final static String INPUT_FILE = "original.txt";
	private static Charset Encoding = StandardCharsets.UTF_8;
	private final static String COMPRESSED_FILE = "cout.txt";
	private final static String DECOMPRESSED_FILE = "dout.txt";
	
	public static void main(String[] args) throws IOException {
	
		EmbeddedAlgorithm e = new EmbeddedAlgorithm();
		String[]binaries =  readFile(INPUT_FILE).toArray(new String[readFile(INPUT_FILE).size()]);
		dictionary= e.makeDictionary(binaries);// Integer.highestbit
		e.compression(binaries,dictionary);
		ArrayList<String> compressed = e.wrapup(binaries); //new ArrayList<String>(Arrays.asList(binaries));// array to arrayList	
		writeInFile(compressed,COMPRESSED_FILE,"compression");
		ArrayList<String> decompressed = readFile(COMPRESSED_FILE);	
		decompressed=e.decompression(decompressed,dictionary);
		writeInFile(decompressed,DECOMPRESSED_FILE,"decompression");
	}
	
	static String maptoString(HashMap<String,String> hm){
		String str ="";
		for(String s:hm.keySet()) str+="\n"+s;
		return str;
	}
	
	static  void writeInFile(ArrayList<String>compressed,String file,String type)throws IOException{
		FileWriter output = new FileWriter(file,true);
		try(BufferedWriter w = new BufferedWriter(output)){
			w.write(compressed.toString().replace(", ","\n").replace("[","").replace("]","")) ;
			w.newLine();
			if(type.equals("compression")){
				w.write("xxxx");
				w.write(maptoString(dictionary));
			}			
		}
	}
	
	static public ArrayList<String> readFile(String filename)throws IOException{
		Path path = null;
		try{
			path = Paths.get(filename);
		}
		catch(Exception e){
			System.out.println("No such file exist");
		}
		return (ArrayList<String>) Files.readAllLines(path,Encoding);
	}
}
class EmbeddedAlgorithm {
		
	String binaryLengthTo(String Binary,int x){
		if(Binary.length() >=x)
			return Binary;
		else{
			while(Binary.length()!=x){
				Binary ="0"+Binary;
			}
			return Binary;
		}	
	}
	
	int integerForm(String str){
		int num = new BigInteger(str, 2).intValue();
		return num;
	}
	
	HashMap<String,String>makeDictionary(String[]binary){
		
		HashMap<String,Integer> dictionary = new LinkedHashMap<String,Integer>();
		for(String s:binary){
			if(dictionary.containsKey(s)){
				dictionary.put(s, (dictionary.get(s)+1) );
			}else
				dictionary.put(s,1);
		}
		HashMap<String,String> result = new LinkedHashMap<String,String>();
		dictionary = sortByValue(dictionary);
		int count = 0;
		for(Map.Entry<String,Integer> hm :dictionary.entrySet()){
			result.put(hm.getKey(),	binaryLengthTo( Integer.toBinaryString(count),3 ));
			count++;
		}
		return result;
	}
	
	public <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue( HashMap<K, V> map ){
	   
		List<Map.Entry<K, V>> list =new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>(){
	        @Override
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	        {
	            return (o2.getValue()).compareTo( o1.getValue() );
	        }
	    } );
	    HashMap<K, V> result = new LinkedHashMap<>();
	    int count = 0;
	    for (Map.Entry<K, V> entry : list){
	    	if(count >7)
	    		break;
	        result.put( entry.getKey(), entry.getValue() );
	        count++;
	    }
	    return result;
	}

	Boolean consequtive(int num){
		String str= Integer.toBinaryString(num);
		if( ( str.lastIndexOf("1")-str.indexOf("1")) >1)
			return false;
		else
			return true;
	}
	
	String posOfSetBitfirst(int num){
		int first = binaryLengthTo(Integer.toBinaryString(num),32).indexOf("1");
		String binaryFirst = Integer.toBinaryString(first);
		return binaryLengthTo(binaryFirst,5);
	}
	
	String posOfSetBitLast(int num){
		int last = binaryLengthTo(Integer.toBinaryString(num),32).lastIndexOf("1");
		String binaryLast = Integer.toBinaryString(last);
		return binaryLengthTo(binaryLast,5);
	}
	
	String minBitChangeString(String binary, HashMap<String,String> map){
		int min = Integer.MAX_VALUE;
		String str=" ";
		for(Map.Entry<String, String> s:map.entrySet()){
			int result = integerForm(binary)^integerForm(s.getKey());
			int count = Integer.bitCount(result);
			if(min>count){
				min = count;
				str = s.getKey();
			}	
		}
		return str;
	}
	
	String[]compression(String[]binaries,HashMap<String,String> dictionary ){
		
		for(int i = 0;i<binaries.length;i++){
			if(dictionary.containsKey(binaries[i])){// direct matching
				binaries[i]="00"+dictionary.get(binaries[i]);
			}
			else if(!dictionary.containsKey(binaries[i])){// 2 mismatch
				String matchStr = minBitChangeString(binaries[i],dictionary);
				int bitschange = integerForm(binaries[i])^integerForm(matchStr);// count the number of difference in bit
				if(Integer.bitCount(bitschange) == 2){
					if(consequtive(bitschange)){
						binaries[i]="01"+posOfSetBitfirst(bitschange)+dictionary.get(matchStr);
					}else{// 2 mismatch anywhere
						binaries[i] ="10"+posOfSetBitfirst(bitschange)+posOfSetBitLast(bitschange)+dictionary.get(matchStr);
					}
				}
				else
					binaries[i] ="11"+binaries[i];
			}
		}
		return binaries;
	}
	
	String paddingBits(String str,int x){
		while(str.length()<x){
			str+="1";
		}return str;	
	}
	
	ArrayList<String> wrapup(String[] binaries){
		String rest= "";
		ArrayList<String>list = new ArrayList<String>();
		for(String s: binaries){
			rest+=s;
		}
		int lastpos = 0;
		while(lastpos<rest.length()-32){
			list.add(rest.substring(lastpos,lastpos+32));	
			lastpos = lastpos+32;
		}
		String last =  paddingBits( (rest.substring(lastpos, rest.length())),32);
		list.add(last);
		return list;
	}
	
	String checkBits(String str, int x){
		if(str.length()>x)
			return str.substring(0,x);
		else
			return "";
	}
	
	String removeBits(String str, int x){
		str = str.substring(x,str.length());
		return str;
	}
	
	String getKey(HashMap<String,String> hm, String value){
		for(String s:hm.keySet()){
			if(value.equals(hm.get(s)))
				return s;
		}
		return "";
	}
	
	String flipBits(String str,int y){

		String s = String.valueOf(str.charAt(y));
		String bit = (s.equals("0"))?"1":"0";
		return str.substring(0,y)+bit+str.substring(y+1);
	}
	
	String decode(String rest,ArrayList<String> result,HashMap<String,String> dictionary){
		if(checkBits(rest,2).equals("00")){
			rest=removeBits(rest,2);
			result.add( (getKey(dictionary,checkBits(rest,3))) );
			rest=removeBits(rest,3);
			
		}else if(checkBits(rest,2).equals("01")){
			rest=removeBits(rest,2);
			int flippos = integerForm( binaryLengthTo( checkBits(rest,5),32 ));
			rest =removeBits(rest,5);
			String str = flipBits((getKey(dictionary,checkBits(rest,3))),flippos);
			str = flipBits(str,(flippos+1));
			result.add( str );
			rest =removeBits(rest,3);
			
		}else if(checkBits(rest,2).equals("10")){
			rest=removeBits(rest,2);
			int flippos1 = integerForm( binaryLengthTo( checkBits(rest,5),32 ));
			rest =removeBits(rest,5);
			int flippos2 = integerForm( binaryLengthTo( checkBits(rest,5),32 ));
			rest =removeBits(rest,5);
			String str = flipBits((getKey(dictionary,checkBits(rest,3))),flippos1);
			str= flipBits(str,flippos2);
			result.add(str);
			rest =removeBits(rest,3);
			
		}else if(checkBits(rest,2).equals("11")){

			rest=removeBits(rest,2);
			result.add(checkBits(rest,32));
			rest=removeBits(rest,32);
		}
		return rest;
	}
	
	ArrayList<String> decompression(ArrayList<String> list,HashMap<String,String> dictionary){
		
		String rest = list.toString().replace(",","").replace("[", "").replace("]", "").replace(" " , "");
		ArrayList<String> result = new ArrayList<String>();
		rest = rest.substring(0, rest.indexOf("x"));
		for(int i=0;i<rest.length();i++){
			rest = decode(rest,result,dictionary);	
		}
		decode(rest,result,dictionary);
		return result;
	}
}