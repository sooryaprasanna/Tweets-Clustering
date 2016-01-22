package part2;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Tweet {
	
	private String id;
	private String tweet;
	
	public Tweet(String id, String tweet){
		this.id =id;
		this.tweet=tweet;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTweet() {
		return tweet;
	}
	public void setTweet(String tweet) {
		this.tweet = tweet;
	}
	
	public double getDistance(Tweet tmpTweet){
		
		HashSet<String> wordSet = new HashSet<String>();
		HashSet<String> wordSet2 = new HashSet<String>();
		int count = 0;
		int intCount = 0;
		for(String tmp : this.tweet.split(" ")){
		
		 if(!wordSet.contains(tmp))
		 {
			 wordSet.add(tmp);
			 count++;
		 }
		 
		}
		for(String tmp : tmpTweet.getTweet().split(" ")){
		
			if(wordSet2.contains(tmp)){
				continue;
			}
			else{
				wordSet2.add(tmp);
			}
			if(!wordSet.contains(tmp))
			 {
				count++;
			 }
			else{
				intCount++;
			}
		}
		double distance = (1.0-((double)intCount/(double)count));
		return distance;
	}
}

class Cluster
{	
	private int id;
	private ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
	private Tweet centroid;
	public Cluster(int id, Tweet centroid){
		this.id =id;
		this.centroid=centroid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<Tweet> getTweetList() {
		return tweetList;
	}
	public void setTweetList(ArrayList<Tweet> pointList) {
		this.tweetList = pointList;
	}
	public Tweet getCentroid() {
		return centroid;
	}
	public void setCentroid(Tweet centroid) {
		this.centroid = centroid;
	}
	
	public boolean recomputeCentroid(){
		Tweet newCent=null;
		double dist=Double.MAX_VALUE;		
		for(Tweet tweet: this.tweetList)
		{
			double tmpDist=0;
			for(Tweet tweetTmp: this.tweetList)
			{
				tmpDist += tweet.getDistance(tweetTmp);	
			}
			if(tmpDist < dist)
			{	
				dist = tmpDist;
				newCent = tweet;
			}
		}
		if(this.getCentroid() != newCent)
		{
			this.setCentroid(newCent);
			return true;
		}		
		return false;
	}
}


public class TweetsClustering {
	
	public static HashMap<String,Tweet> tweetList; 
	public static ArrayList<Cluster> clusterList;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(args.length == 0){
			System.out.println("Pass arguments as : <numberOfClusters> <initialSeedsFile> <input-file-name> <output-file-name>");
			System.exit(0);
		}
		
		int k =Integer.parseInt(args[0]);
		String initialSeed =args[1];
		String fileName=args[2];
		String outputFile =args[3];
		//String fileName = "Tweets.json";
		//String initialSeed ="InitialSeeds.txt";
		//String outputFile ="ResultTweet.txt";
		//int k = 25;
		
		if(k>25){
		
			k=25;
		}
		tweetList =new HashMap<String,Tweet>();
		clusterList =new ArrayList<Cluster>();
		TweetsClustering kmean =new TweetsClustering();
		kmean.init(fileName,initialSeed, k);
		kmean.populateCluster();
		kmean.computeCluster();
		kmean.print(outputFile);
		kmean.sse();
		
				
	}
	
	
public void init(String fileName,String initialSeed, int k){
		
		
		String fileDir = System.getProperty("user.dir");
		String fileSeperator=System.getProperty("file.separator");
		StringBuilder builder = new StringBuilder(fileDir);
		builder.append(fileSeperator+"part2"+fileSeperator);

		
		String tweetPat = "(\"text\": .*?\", )";
		String tweetIdPat = "(\"id\": \\d*,)";
		Pattern regPat1 = Pattern.compile(tweetPat);
		Pattern regPat2 = Pattern.compile(tweetIdPat);
		String line =null;
		Tweet centTweet=null;
		
		try{
			BufferedReader bufferedReader = 
	                new BufferedReader(new FileReader(builder.toString()+fileName));
            
			String text="";
			String id="";
	            while((line = bufferedReader.readLine()) != null) {
	            	
	            	Matcher m1 = regPat1.matcher(line);
	            	Matcher m2 = regPat2.matcher(line);
	            	while(m1.find()){
	            		text=m1.group(0);
	            		text=text.substring(9,text.length()-3);
	            		text = text.replaceAll("[^a-zA-Z0-9 -]", "");
	            	}
	            	while(m2.find()){
	            		id= m2.group(0);
	            		id =id.substring(6,id.length()-1);
	            		
	            	}
	            	
	            	tweetList.put(id,new Tweet(id, text));
	            }  
	            bufferedReader.close();  
	            
	            BufferedReader bufferedReader2 = 
		                new BufferedReader(new FileReader(builder.toString()+initialSeed));
	                int count =1;
				    while((line = bufferedReader2.readLine()) != null) {
				    	if(line.endsWith(","))
				    		{
				    		line = line.substring(0, line.length()-1);	
				    		}
		            centTweet=tweetList.get(line);
		            if(count <= k){
		            	clusterList.add(new Cluster(count,centTweet));
		            	count++;
		            }
		            else{
		            	break;
		            }
		            }  
		            bufferedReader2.close();   
			}catch(FileNotFoundException ex) {
				 ex.printStackTrace();    
	            }
	            catch(IOException ex) {
	               ex.printStackTrace();
	            }
	}

	public void populateCluster() {

		Iterator it = tweetList.entrySet().iterator();
		Map.Entry pair = null;
		Tweet tweet;
		while (it.hasNext()) {

			pair = (Map.Entry) it.next();
			tweet = (Tweet) pair.getValue();
			Cluster bestCluster = null;
			double dist = 999999.99;
			for (Cluster cluster : clusterList) {

				double tmpDist = tweet.getDistance(cluster.getCentroid());
				if (tmpDist < dist) {
					dist = tmpDist;
					bestCluster = cluster;
				}

			}
			bestCluster.getTweetList().add(tweet);
		}

	}
	
	public void computeCluster(){
		
		boolean change =false;	
		int count = 0;
		do{
			count++;
		change =false;
		for(Cluster cluster : clusterList){
			
			if(cluster.recomputeCentroid()){
				change =true;
			}
		}
		if(change){
			
			for(Cluster cluster : clusterList){
				
				cluster.getTweetList().clear();
			}
			
		populateCluster();	
		}
		}while(change);
		
	}

	public void print(String output){
		
		String fileDir = System.getProperty("user.dir");
		String fileSeperator=System.getProperty("file.separator");
		StringBuilder builder = new StringBuilder(fileDir);
		builder.append(fileSeperator+"part2"+fileSeperator+output);

		PrintWriter writer;
		try {
			writer = new PrintWriter(builder.toString(), "UTF-8");
			
			for(Cluster cluster: clusterList){
				int pointCount = cluster.getTweetList().size();
				writer.print(cluster.getId()+" : ");
				for (Tweet tweet: cluster.getTweetList()){
					if(pointCount!=1){
						writer.print(tweet.getId()+",");	
					}
					else{
						writer.print(tweet.getId());
					}
					pointCount--;
				}
				writer.println("");
			}
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sse(){
		
		double distSq = 0;
		for(Cluster cluster: clusterList){
	    	
			
			Tweet centroid = cluster.getCentroid();  
	    	for(Tweet tweet: cluster.getTweetList()){
	    		
	    		double dist = tweet.getDistance(centroid);
	    		distSq+= Math.pow(dist, 2);
	    	    	
	    	}
	    	
	    }
		System.out.println("SSE : "+distSq);
		
	}	
}