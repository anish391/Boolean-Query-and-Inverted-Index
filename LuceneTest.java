package com.ir.pubmed;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.Collections;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;

public class LuceneTest {
	static String dir = "C:\\Users\\Anish\\Desktop\\Suny Buffalo\\Information Retrieval\\Project 2";
	static String index = "index";
	static int comparisons_counter=0;
	
	public static LinkedList<Integer> intersectTaatAnd(LinkedList<Integer> a, LinkedList<Integer> b) {
		LinkedList<Integer> answer = new LinkedList<>();
		//comparisons_counter = 0;
		int x=0,y=0;
		//System.out.println(a);
		//System.out.println(b);
			
		
		while(x != a.size() && y != b.size()) {
			int docA=a.get(x);
			int docB=b.get(y);
			if(docA == docB) {
				comparisons_counter++;
				answer.add(docA);
				x++;
				y++;
			}
			else if(a.get(x) < b.get(y)) { 
				comparisons_counter++;
				x++;
			}
			else {
				comparisons_counter++;
				y++;
			}
		}
		return answer;
	}
	
	public static LinkedList<Integer> intersectTaatOr(LinkedList<Integer> a, LinkedList<Integer> b) {
		LinkedList<Integer> answer = new LinkedList<>();
		//comparisons_counter = 0;
		int x=0,y=0;
		//System.out.println(a);
		//System.out.println(b);
		while(x != a.size() && y != b.size()) {
			int docA=a.get(x);
			int docB=b.get(y);
			if(docA == docB) {
				comparisons_counter++;
				if(!answer.contains(docA))
					answer.add(docA);
				x++;
				y++;
			}
			else if(docA < docB) { 
				comparisons_counter++;
				if(!answer.contains(docA))
					answer.add(docA);
				x++;
			}
			else {
				comparisons_counter++;
				if(!answer.contains(docB))
					answer.add(docB);
				y++;
			}
		}
		if(x!=a.size() || y!=b.size()) {
			if(x!=a.size()) {
				while(x!=a.size()) {
					int docA = a.get(x);
					if(!answer.contains(docA)) {
						comparisons_counter++;
						answer.add(docA);
					}
					x++;
				}
			}
			else if(y!=b.size()) {
				while(y!=b.size()) { 
					int docB = b.get(y);
					if(!answer.contains(docB)) {
						comparisons_counter++;
						answer.add(docB);
					}
					y++;
				}
			}
		}
		return answer;
	}
	
	public static void main(String args[]) throws IOException {
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(dir, index)));
		Fields fields = MultiFields.getFields(indexReader);
		System.out.println(indexReader.numDocs());
		Map<String, LinkedList<Integer>> dictionary = new HashMap<>();
		Iterator<String> fieldIterator = fields.iterator();
		String field= fieldIterator.next();
		while(fieldIterator.hasNext()) {
			field = fieldIterator.next();
			Terms terms = fields.terms(field);		
			TermsEnum termsIterator = terms.iterator();
			PostingsEnum posting = null;
			while(termsIterator.next() != null){
				BytesRef termBytes = termsIterator.term();
				String termString = termBytes.utf8ToString();
				LinkedList<Integer> termPosting = new LinkedList<>();
				posting = termsIterator.postings(posting);
				int docId;
				do{
					docId = posting.nextDoc();
					if(docId == 2147483647)
						continue;
					termPosting.add(docId);
				} while(docId != DocIdSetIterator.NO_MORE_DOCS);
				Collections.sort(termPosting);
				dictionary.put(termString, termPosting);
			}	
		}
		TreeMap<String, LinkedList<Integer>> sortedDictionary = new TreeMap<>();
		sortedDictionary.putAll(dictionary);
		System.out.println(dictionary.size());
		FileWriter sample = new FileWriter(new File("index.txt"));
		BufferedWriter bw = new BufferedWriter(sample);
		for(String name: sortedDictionary.keySet()) {
			String key = name;
			String value = sortedDictionary.get(name).toString();
			bw.write(key+ " " +value+"\n");
		}
		bw.close();
		// Implementation of Daat and Taat
		BufferedReader ip = null;
		FileWriter fw = null;
		BufferedWriter op = null;
		
		try {
			ip = new BufferedReader(new FileReader(new File("input.txt")));
			fw = new FileWriter(new File("output.txt"));
			op = new BufferedWriter(fw);
			System.out.println("Success");
			for(String x = ip.readLine(); x!=null; x=ip.readLine()) {
				byte[] bytesArray = x.getBytes();
				String y = new String(bytesArray,"UTF-8");
				String[] printTerms = y.split(" ");
				String[] termIp = y.split(" ");
				
				//GetPostings
				for(String term: termIp) {
					op.write("GetPostings\n");
					op.write(term+"\nPostings List: ");
					LinkedList<Integer> getPosting = sortedDictionary.get(term);
					for(int i=0;i<getPosting.size();i++)
						op.write(getPosting.get(i)+" ");
					op.write("\n");
				}
				//Sort by increasing term frequency
				for(int i=0;i<termIp.length-1;i++) {
					String temp;
					for(int j=0;j<termIp.length-i-1;j++) {
						LinkedList<Integer> getPostingI = sortedDictionary.get(termIp[j]);
						LinkedList<Integer> getPostingJ = sortedDictionary.get(termIp[j+1]);
						if(getPostingI.size() < getPostingJ.size()) {
							temp = termIp[j];
							termIp[j] = termIp[i];
							termIp[i] = temp;
						}
					}
				} 
				//TaatAnd
				op.write("TaatAnd\n");
				for(String term: printTerms) {
					op.write(term+" ");
				}
				op.write("\n");
				LinkedList<Integer> a = sortedDictionary.get(termIp[0]);
				for(int i = 1; i<termIp.length;i++) {
					LinkedList<Integer> b = sortedDictionary.get(termIp[i]);
					a = intersectTaatAnd(a,b);
				}
				op.write("Results: ");
				if(a.size()==0)
					op.write("empty\n");
				else {
					for(int j=0;j<a.size();j++) {
						op.write(a.get(j)+" ");	
					}
					op.write("\n");
				}
				op.write("Number of documents in results: "+a.size()+"\n");
				op.write("Number of comparisons: "+comparisons_counter+"\n");
				comparisons_counter=0;
				
				//TaatOr
				
				a.clear();
				op.write("TaatOr\n");
				for(String term: printTerms) {
					op.write(term+" ");
				}
				op.write("\n");
				LinkedList<Integer> answer = new LinkedList<>();
				a = sortedDictionary.get(termIp[0]);
				for(int i = 1; i<termIp.length;i++) {
					LinkedList<Integer> b = sortedDictionary.get(termIp[i]);
					answer.addAll(intersectTaatOr(a,b));
				}
				op.write("Results: ");
				if(answer.size()==0)
					op.write("empty\n");
				else {
					for(int j=0;j<answer.size();j++) {
						op.write(answer.get(j)+" ");	
					}
					op.write("\n");
				}
				op.write("Number of documents in results: "+answer.size()+"\n");
				op.write("Number of comparisons: "+comparisons_counter+"\n");
				comparisons_counter=0;
				
				//DaatAnd
				
				a.clear();
				
				
				
				
				
				
				
			} // for loop end
			op.flush();
		}catch (IOException e){
			System.out.println("Incorrect input file");
		}finally {
			try {
				ip.close();
				fw.close();
				op.close();
			}catch(IOException e) {
				System.out.println("Failure.");
			}
		}
	} // main
	
}
