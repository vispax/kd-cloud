package com.kdcloud.server.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import com.kdcloud.weka.core.Attribute;
import com.kdcloud.weka.core.DenseInstance;
import com.kdcloud.weka.core.Instances;


public class QRSTest {
	
	public static final int M = 5;
	public static final int windowsSize=15;
	public static final int sampling_rate_ms=10;
	//public static final String file_name="samples/ecg/07.txt";

	public static double[] highPass(double[] sig0, int nsamp) {
        double[] highPass = new double[nsamp];
        double constant = (double) 1/M;
 
        for(int i=0; i<sig0.length; i++) {
            double y1 = 0;
            double y2 = 0;
 
            int y2_index = i-((M+1)/2);
            if(y2_index < 0) {
                y2_index = nsamp + y2_index;
            }
            y2 = sig0[y2_index];
 
            double y1_sum = 0;
            for(int j=i; j>i-M; j--) {
                int x_index = i - (i-j);
                if(x_index < 0) {
                    x_index = nsamp + x_index;
                }
                y1_sum += sig0[x_index];
            }
            y1 = constant * y1_sum;
            highPass[i] = y2 - y1;
        }        
        return highPass;
    }
	
	 public static double[] lowPass(double[] sig0, int nsamp) {
	        double[] lowPass = new double[nsamp];
	        for(int i=0; i<sig0.length; i++) {
	            double sum = 0;
	            if(i+windowsSize < sig0.length) {
	                for(int j=i; j<i+windowsSize; j++) {
	                    double current = sig0[j] * sig0[j];
	                    sum += current;
	                }
	            }
	            else if(i+windowsSize >= sig0.length) {
	                int over = i+windowsSize - sig0.length;
	                for(int j=i; j<sig0.length; j++) {
	                    double current = sig0[j] * sig0[j];
	                    sum += current;
	                }
	                for(int j=0; j<over; j++) {
	                    double current = sig0[j] * sig0[j];
	                    sum += current;
	                }
	            }
	            lowPass[i] = sum;
	        }
	        return lowPass;
	    }
	 
	 	public static double average(double[] array){
	 		double sum=0;
	 		for(int i=0; i<array.length;i++){
	 			sum+=array[i];
	 		}
	 		return sum/array.length;
	 	}
	 
	 	public static int[] QRS_RC(double[] lowPass, int nsamp) {
	 		int[] QRS = new int[nsamp];
	 		
	 		int cut_length=200;
	 		double[] sort_array=Arrays.copyOf(lowPass, cut_length); 
	 		Arrays.sort(sort_array); 
	 		int percent=50*cut_length/100;
	 		double[] mean_array=Arrays.copyOfRange(sort_array,(sort_array.length-percent),sort_array.length); 
	        double treshold =average(mean_array);
	 		
	 		/*int cut_length=100;
	 		double max_value=getMax(lowPass,0,cut_length);
	 		double treshold =max_value - (max_value*80/100);*/
	 
	        int frame = 300;//400 is good too
	        int i=0;
	 
	        while(i<lowPass.length){
	            double max = 0;
	            int index = 0;
	            
	            if(i + frame > lowPass.length) {
	                index = lowPass.length;
	            }
	            else {
	                index = i + frame;
	            }
	            
	            int offset= 0;
	            
	            if(lowPass[index-1]>treshold && index<lowPass.length){
	            	double low_th=(treshold*90)/100;
	            	while(lowPass[index-1]>(treshold-low_th) && index<lowPass.length){
			        	index+=1;
			        	offset+=1;
		            }
	            }
	
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] > max) max = lowPass[j];
	            }
	            double last_max=0;
            	int index_last_max=-1;
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] >= treshold && lowPass[j]>last_max) {
	                   last_max=lowPass[j];
	                   index_last_max=j;
	                }else if(lowPass[j] < treshold && index_last_max!=-1){
	                	QRS[index_last_max] = 1;
	                	last_max=0;
	                	index_last_max=-1;
	                }
	            }	 
	            
	            double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
	            double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
	            
	            treshold = alpha * gama * max + (1 - alpha) * treshold;
	            
	            i+=frame+offset;
	        }
	 
	        return QRS;
	 	}
	 	
	 	public static double getMax(double[] array, int start, int end){
	 		double max=array[start];
	 		for(int i=(start+1);i<end;i++){
	 			if(array[i]>max)max=array[i];
	 		}
	 		return max;
	 	}
	 	
	 	public static int[] qrs_rc_ver2(double[] lowPass, int nsamp) {
	 		int[] QRS = new int[nsamp];
	 		
	 		int cut_length=100;
	 		double[] sort_array=Arrays.copyOf(lowPass, cut_length); 
	 		Arrays.sort(sort_array); 
	 		int percent=50*cut_length/100;
	 		double[] mean_array=Arrays.copyOfRange(sort_array,(sort_array.length-percent),sort_array.length); 
	        double treshold =average(mean_array);
	 		
	 		/*int cut_length=100;
	 		double max_value=getMax(lowPass,0,cut_length);
	 		double treshold =max_value - (max_value*80/100);*/
	 
	        int frame = 300;//400 is good too
	        int i=0;
	 
	        while(i<lowPass.length){
	            double max = 0;
	            int index = 0;
	            
	            if(i + frame > lowPass.length) {
	                index = lowPass.length;
	            }
	            else {
	                index = i + frame;
	            }
	            
	            int offset= 0;
	            
	            double low_th1=(treshold*90)/100;
	            double low_th2=(treshold*70)/100;
	            
	            if(lowPass[index-1]>treshold && index<lowPass.length){	
	            	while(lowPass[index-1]>(treshold-low_th1) && index<lowPass.length){
			        	index+=1;
			        	offset+=1;
		            }
	            }
	
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] > max) max = lowPass[j];
	            }
	            double last_max=0;
            	int index_last_max=-1;
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] >= treshold && lowPass[j]>last_max) {
	                   last_max=lowPass[j];
	                   index_last_max=j;
	                }else if(lowPass[j] < treshold && index_last_max!=-1 && lowPass[j] < (treshold-low_th2)){
	                	QRS[index_last_max] = 1;
	                	last_max=0;
	                	index_last_max=-1;
	                }
	            }
	            
	            double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
	            double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
	            
	            treshold = alpha * gama * max + (1 - alpha) * treshold;
	            
	            if(index_last_max!=-1 && lowPass[index-1] < treshold){
	            	QRS[index_last_max] = 1;
	            }
	            
	            i+=frame+offset;
	        }
	 
	        return QRS;
	 	}
	 
	    public static int[] QRS(double[] lowPass, int nsamp) {
	        int[] QRS = new int[nsamp];
	 
	        double treshold = 0;
	 
	        for(int i=0; i<200; i++) {
	            if(lowPass[i] > treshold) {
	                treshold = lowPass[i];
	            }
	        }
	 
	        int frame = 250;
	 
	        for(int i=0; i<lowPass.length; i+=frame) {
	            double max = 0;
	            int index = 0;
	            if(i + frame > lowPass.length) {
	                index = lowPass.length;
	            }
	            else {
	                index = i + frame;
	            }
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] > max) max = lowPass[j];
	            }
	            boolean added = false;
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] > treshold && !added) {
	                    QRS[j] = 1;
	                    added = true;
	                }
	                else {
	                    QRS[j] = 0;
	                }
	            }
	 
	            double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
	            double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
	            
	            treshold = alpha * gama * max + (1 - alpha) * treshold;
	 
	        }
	 
	        return QRS;
	    }
	    
	    public static double[] readData(File file){
	    	BufferedReader in = null;
			Vector<Double> sign=new Vector<Double>();
			try {
				in = new BufferedReader(new FileReader(file));
				String line = in.readLine();
				while(line!=null){
					//System.out.print(line+"\t");
					//System.out.println(Double.parseDouble(line));
					sign.add(Double.parseDouble(line));
					line = in.readLine();
				};
				in.close();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double [] res=new double[sign.size()];
			for(int i=0;i<sign.size();i++){
				res[i]=sign.get(i);
			}
			return res;
	    }
	
	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		URI uri = QRSTest.class.getClassLoader().getResource("ecg_test.txt").toURI();
		double[] sign = readData(new File(uri));
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(new Attribute("sign"));
		Instances data = new Instances("test", attrs, 20000);
		for (int i = 0; i < sign.length; i++) {
			double[] row = {sign[i]};
			data.add(new DenseInstance(0, row));
		}
		Instances res = QRS.ecg(data);
		System.out.println(data.numInstances());
		System.out.println(res.numInstances());
	}
		

}