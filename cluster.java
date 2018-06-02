import java.awt.Point;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class cluster {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String linkType = args[1].trim();
		int clusterNum = Integer.parseInt(args[2].trim());
		String[][] expressionArray = null;

		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(args[0].trim()), StandardCharsets.UTF_8);
	
			expressionArray = new String[lines.size()][]; 
	
			for(int i =0; i<lines.size(); i++){
				expressionArray[i] = lines.get(i).split("\t"); //tab-separated
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Construct Average Array
		Double[] averageA = new Double[expressionArray.length];
		for(int i =0; i < expressionArray.length; i++){
			Double sum = 0.0;
			Integer count = 0;
			for(int j=2; j<expressionArray[i].length; j ++){
				sum += Double.parseDouble(expressionArray[i][j]);
				count ++;
			}
			averageA[i] = sum/count;
		}
		
		// Construct the initial Distance Matrix
		Double[][] distanceM = new Double[expressionArray.length][expressionArray.length];
		
		for(int i =0; i < distanceM.length; i ++){
			distanceM[i][i] = 0.0;
		}
		
		for(int i = 0; i < expressionArray.length; i ++) {
			for(int j = i + 1; j < expressionArray.length; j++) {
				double sum = 0.0;
				for(int k = 2; k < expressionArray[i].length; k++) {
					sum += Math.pow((Double.parseDouble(expressionArray[i][k]) - Double.parseDouble(expressionArray[j][k])), 2);
				}
				double ecdDis = Math.sqrt(sum);
				distanceM[i][j] = ecdDis;
			}
		}
		
		//Construct clusterM to keep track of how many cluster and which index is in which cluster
		ArrayList<ArrayList<Integer>> clusterM = new ArrayList<ArrayList<Integer>>();
		for(int i =0; i < distanceM.length; i ++){
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(i);
			clusterM.add(temp);
		}

		// Cluster the shit out of the data
		while(clusterM.size() > clusterNum) {
		   Point argMinIndex = findArgMin(distanceM, linkType);
		   ArrayList<Integer> oldArr = clusterM.get(argMinIndex.y);
		   for(int i =0; i < oldArr.size(); i ++){
			   clusterM.get(argMinIndex.x).add(oldArr.get(i));
		   }
		   
		   clusterM.remove(argMinIndex.y);
		   Double[][] newDistanceM = new Double[clusterM.size()][clusterM.size()];
		   for(int i =0; i < newDistanceM.length; i ++){
			   newDistanceM[i][i] = 0.0;
		   }
		   int oldDMI = 0;
		   int oldDMJ = oldDMI + 1;
		   for(int i =0; i < newDistanceM.length; i ++){
			   if(oldDMI == argMinIndex.y){
				   oldDMI +=1;
				   oldDMJ +=1;
			   }
			   for(int j = i + 1; j < newDistanceM[i].length; j++){
				   if(oldDMI != argMinIndex.x && oldDMJ != argMinIndex.x){
					   if(oldDMJ == argMinIndex.y){
						   oldDMJ +=1;
					   }
					   newDistanceM[i][j] = distanceM[oldDMI][oldDMJ];
				   } else{
					   double updatedDistance = 0.0;
					   double oneInd = 0.0;
					   double twoInd = 0.0;
					   if(oldDMI != argMinIndex.x){ 
						   if(oldDMI < argMinIndex.x) {
							   oneInd = distanceM[oldDMI][argMinIndex.x];
						   } else {
							   oneInd = distanceM[argMinIndex.x][oldDMI];
						   }
						   
						   if(oldDMI < argMinIndex.y) {
							   twoInd = distanceM[oldDMI][argMinIndex.y];
						   } else {
							   twoInd = distanceM[argMinIndex.y][oldDMI];
						   }
					   } else {
						   if(oldDMJ == argMinIndex.y){
							   oldDMJ +=1;
						   }
						   if(oldDMJ < argMinIndex.x) {
							   oneInd = distanceM[oldDMJ][argMinIndex.x];
						   } else {
							   oneInd = distanceM[argMinIndex.x][oldDMJ];
						   }
						   
						   if(oldDMJ < argMinIndex.y) {
							   twoInd = distanceM[oldDMJ][argMinIndex.y];
						   } else {
							   twoInd = distanceM[argMinIndex.y][oldDMJ];
						   }
					   }
					   
					   updatedDistance = Math.min(oneInd, twoInd); 
					   if(linkType.equals("A")){
						   updatedDistance = (oneInd + twoInd)/2;
					   } else if(linkType.equals("C")){
						   updatedDistance = Math.max(oneInd, twoInd); 
					   } else if(linkType.equals("S")){
						   updatedDistance = Math.min(oneInd, twoInd); 
					   }
					   newDistanceM[i][j] = updatedDistance; 
				   }
				   oldDMJ +=1;
			   }
			   oldDMI +=1;
			   oldDMJ = oldDMI +1;
		   }
		   distanceM = newDistanceM;		
		}
		
		ArrayList<clusObj> clusObjArr = new ArrayList<clusObj>(); 
		for(int i =0; i < clusterM.size(); i ++){
			ArrayList<Integer> temp = clusterM.get(i);
			Collections.sort(temp);
			ArrayList<String> nama = new ArrayList<String>();
			ArrayList<Double> averages = new ArrayList<Double>();
			for(int j =0; j < temp.size(); j++){
				String name = expressionArray[temp.get(j)][0] + " " + expressionArray[temp.get(j)][1];
				Double avg = averageA[temp.get(j)];
				nama.add(name);
				averages.add(avg);
			}
			clusObj newClus = new clusObj(nama, averages);
			clusObjArr.add(newClus);
		}
		Collections.sort(clusObjArr, sortClusByTotalAvg());
		for(int i =0; i < clusObjArr.size(); i++){
			clusObj currClus = clusObjArr.get(i);
			ArrayList<String> combi = currClus.getSortedArr();
			for(int j =0; j < combi.size(); j++){
				System.out.println(combi.get(j));
			}
			System.out.printf("%.3f", currClus.getTotalAvg());
			System.out.println();
			System.out.println();
		}
		
	}
	
	public static Comparator<clusObj> sortClusByTotalAvg(){
		Comparator<clusObj> comp = new Comparator<clusObj>(){
		     @Override
		     public int compare(clusObj s1, clusObj s2)
		     {
		    	 Double s1Avg = s1.getTotalAvg();
		    	 Double s2Avg = s2.getTotalAvg();
		         return s1Avg.compareTo(s2Avg);
		     }        
		 };
		 return comp;
	}
	
	public static Point findArgMin(Double distanceM[][], String linkType){
		Point min = new Point(0, 1); 

		for(int row =0; row < distanceM.length; row++) {
		    for(int col= row + 1; col < distanceM[row].length; col++) {
		        if(distanceM[row][col] < distanceM[min.x][min.y])
		            {min.x = row; min.y = col;}
		    } 
		}
		
		return min;
	}

}

class clusObj {
	private ArrayList<String> combi;
	private Double totalAvg;
	public clusObj(ArrayList<String> nama, ArrayList<Double> averages){
		combi = new ArrayList<String>();
		for(int i =0; i < nama.size(); i ++){
			combi.add(nama.get(i) + " " + averages.get(i));
		}
		Collections.sort(combi, sortStringByAvg());
		ArrayList<String> finalCombi = new ArrayList<String>();
		
		for(int i =0; i < combi.size(); i ++){
			String[] temp = combi.get(i).split(" ");
			Double tempAvg = Double.parseDouble(temp[temp.length-1]);
			temp[temp.length -1] = String.format("%.3f", tempAvg);
			finalCombi.add((String.join(" ", temp).trim()));
		}
		this.combi = finalCombi;
		
		
		Double sum = 0.0;
		Integer count = 0;
		for(int i =0; i < averages.size(); i ++){
			sum += averages.get(i);
			count++;
		}
		totalAvg = sum/count;
	}
	
	public Double getTotalAvg(){
		return this.totalAvg;
	} 
	
	// remember to sort array with indexes first 
	public ArrayList<String> getSortedArr(){
		return this.combi;
	}
	
	public Comparator<String> sortStringByAvg(){
		Comparator<String> comp = new Comparator<String>(){
		     @Override
		     public int compare(String s1, String s2)
		     {
		    	 String[] s1Split = s1.split(" ");
		    	 String[] s2Split = s2.split(" ");
		    	 Double s1Avg = Double.parseDouble(s1Split[s1Split.length - 1]);
		    	 Double s2Avg = Double.parseDouble(s2Split[s2Split.length - 1]);
		         return s1Avg.compareTo(s2Avg);
		     }        
		 };
		 return comp;
	}
	
}
