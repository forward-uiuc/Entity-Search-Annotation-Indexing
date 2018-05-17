package org.forward.entitysearch.ner.annotation.extraction.huawei_tagging;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
public class ChineseQuestionTagging {

	public ChineseQuestionTagging()
	{
		//empty constructor
	}
	
	public static boolean isNumeric(String str){  
		  for (int i = str.length();--i>=0;){    
		   if (!Character.isDigit(str.charAt(i))){  
		    return false;  
		   }  
		  }  
		  return true;  
		} 
	
	
	 
	// 判断一个字符是否是中文  
	    public static boolean isChinese(char c) {  
	        return c >= 0x4E00 &&  c <= 0x9FA5;// 根据字节码判断  
	    }  
	    // 判断一个字符串是否含有中文  
	    public static boolean isChinese(String str) {  
	        if (str == null) return false;  
	        for (char c : str.toCharArray()) {  
	            if (isChinese(c)) return true;// 有一个中文字符就返回  
	        }  
	        return false;  
	    }
	    
	    public static void allDictionaryToTable1(String[]paths,String resultPath) throws Exception
		{
			Map<String,Set<String>> phrasetype=new HashMap<String,Set<String>>();
			Map<String,Set<String>> phraseLabel=new HashMap<String,Set<String>>();
			Map<String,String> type2index=new TreeMap<String,String>();
			BufferedWriter bwsc=null;
		    //OutputStreamWriter  sc=new OutputStreamWriter(new FileOutputStream("Q:\\huawei\\dictionary\\professionalwords_Id_Type_dictionary.csv"),"UTF-8");
		    OutputStreamWriter  sc=new OutputStreamWriter(new FileOutputStream(resultPath+"_phraseIndex"),"UTF-8");
		    bwsc=new BufferedWriter(sc);
	        FileReader fileinput = null;
			BufferedReader br=null;
			Set<String> phrases=new HashSet<String>();
			Map<String,Set<String>> phrase2alias=new HashMap<String,Set<String>>();
			Map<String,Map<String,String>> alias2Properties=new HashMap<String,Map<String,String>>();
			
			int no=0;
			
				try {
					for(String path: paths)
					{
					InputStreamReader read =null;
					//fileinput = new FileReader(read);
					
					String s=null;
					
					File file=new File(path);
					 read = new InputStreamReader(new FileInputStream(file),"UTF-8"); 
					 br=new BufferedReader(read);
				
				
					String filename=file.getName();
					if(filename.indexOf(".")>0)
					{
						filename=filename.split("\\.")[0];
					}
					String lastlexicalname="";
					while((s=br.readLine())!=null)
					{
						s=s.toLowerCase();
						//分类一	分类二	分类三	分类四	词类名	同义词
						//table: dictionary: phrase id--->phrase--》词根-》分类信息--> lable(such as, product, application)--> version
						
						
						if(s.split("\t").length<5)
						{
							continue;
							//System.out.println(s);
						}
						
						String t1=s.split("\t")[0].trim();
						String t2=s.split("\t")[1].trim();
						String t3=s.split("\t")[2].trim();
						String t4=s.split("\t")[3].trim();
						String type=t1+"<#>"+t2+"<#>"+t3+"<#>"+t4;
						String lexicalname=s.split("\t")[4];
						no++;
						if(lexicalname!=null&&!lexicalname.equals(""))
						{
							Set<String> set=new HashSet<String>();
							if(phrasetype.containsKey(lexicalname))
							{
								set=phrasetype.get(lexicalname);
							}
							set.add(type);
							phrasetype.put(lexicalname, set);
							
							Set<String> labelSet=new HashSet<String>();
							if(phraseLabel.containsKey(lexicalname))
							{
								labelSet=phraseLabel.get(lexicalname);
							}
							
							
							
							labelSet.add(filename);
							phraseLabel.put(lexicalname, labelSet);
							
							Map<String,String> map=new HashMap<String,String>();
							if(alias2Properties.containsKey(lexicalname))
							{
								map=alias2Properties.get(lexicalname);
							}
							String aliasroot=lexicalname;
							map.put("phraseroot", aliasroot);
							if(map.containsKey("label"))
							{
								String label=map.get("label");
								if(!label.contains(filename))
								{
									map.put("label", map.get("label")+"<#>"+filename);
								}
							}
							else
							{
								map.put("label", filename);
							}
							
							if(map.containsKey("type"))
							{
								map.put("type", map.get("type")+"<#>"+type);
							}
							else
							{
								map.put("type", type);
							}
							
							alias2Properties.put(lexicalname, map);
							
							
							
							
							
							lastlexicalname=lexicalname;
						}
						else
						{
							String alias=s.split("\t")[5];
							if(alias!=null&&!alias.equals(""))
							{
								// alias filename
								Map<String,String> map=new HashMap<String,String>();
								if(alias2Properties.containsKey(alias))
								{
									map=alias2Properties.get(alias);
								}
								String aliasroot=lastlexicalname;
								map.put("phraseroot", aliasroot);
								if(map.containsKey("label"))
								{
									String label=map.get("label");
									if(!label.contains(filename))
									{
										map.put("label", map.get("label")+"<#>"+filename);
									}
									
								}
								else
								{
									map.put("label", filename);
								}
								
								if(map.containsKey("type"))
								{
									map.put("type", map.get("type")+"<#>"+type);
								}
								else
								{
									map.put("type", type);
								}
								
								alias2Properties.put(alias, map);
														
							}
							
							
						}
						
						
					
					
					}
					
					}
					
					
					int count=0;
					String version="v3";
					for(Entry<String,Map<String,String>> entry: alias2Properties.entrySet())
					{
						String alias=entry.getKey();
						Map<String,String> map=entry.getValue();
						String label=map.get("label");
						String type=map.get("type");
						String phraseroot=map.get("phraseroot");
						count++;
						bwsc.write(count+"\t"+new String(alias.getBytes("UTF-8"))+"\t"+new String(phraseroot.getBytes("UTF-8"))+"\t"+label+"\t"+type+"\t"+version+"\n");
						bwsc.flush();
					}
					
					
					
				
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				finally
				{
					if(br!=null)
					{
					try {
						br.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
					br=null;
					}
					
					System.out.println("success=========================ptxe");
				}
				
		
			
			  	
	    	

	}
	
	public static void questiontagbyAlldictionaryNoOverlap(String []paths,String resultPath,String filePath) throws Exception
	{
			allDictionaryToTable1(paths,resultPath);
			
			FileReader fileinput = null;
				BufferedReader br=null;
			Map<String,List<String>> phrase2infoFourDic=new HashMap<String,List<String>>();
			Map<String,List<String>> phrase2infoGeneralDic=new HashMap<String,List<String>>();
			Map<String,List<String>> phrase2infoProfessionalDic=new HashMap<String,List<String>>();
			Set<String> importantDoc=new HashSet<String>();
			Set<String> secondImportantDoc=new HashSet<String>();
						
				try {
					fileinput = new FileReader(resultPath+"_phraseIndex");
					br=new BufferedReader(fileinput);
					String s=null;
					
					while((s=br.readLine())!=null){
						
						//125	互联网电视	互联网电视	professionalwords<#>accessory	针对性同义词*<#>重点词*<#>相关名词<#>	2017-11-30
						s=s.toLowerCase();
						String phraseno=s.split("\t")[0];
						String phrase=s.split("\t")[1];
						String phraseroot=s.split("\t")[2];
						String phraselabel=s.split("\t")[3];
						String phrasetype=s.split("\t")[4];
						if(phraselabel.contains("product")||phraselabel.contains("symptom")||phraselabel.equals("a")||phraselabel.contains("a<#>")||phraselabel.contains("<#>a"))
						{
							
							String phraseversion=s.split("\t")[5];
							List<String> list=new ArrayList<String>();
							list.add(phraseno);
							list.add(phraseroot);
							if(phraselabel.contains("<#>professionalwords"))
							{
								phraselabel=phraselabel.replace("<#>professionalwords", "");
							}
							if(phraselabel.contains("professionalwords<#>"))
							{
								phraselabel=phraselabel.replace("professionalwords<#>", "");
							}
							
							if(phraselabel.contains("<#>generalwords"))
							{
								phraselabel=phraselabel.replace("<#>generalwords", "");
							}
							if(phraselabel.contains("generalwords<#>"))
							{
								phraselabel=phraselabel.replace("generalwords<#>", "");
							}
							
							list.add(phraselabel);
							list.add(phrasetype);
							list.add(phraseversion);
							if(!isNumeric(phrase))
							{
								phrase2infoFourDic.put(phrase, list);
								importantDoc.add(phrase);
							}
							//phrase2infoFourDic.put(phrase, list);
						}
						
							
									
											
					}
					
					
					fileinput = new FileReader(resultPath+"_phraseIndex");
					br=new BufferedReader(fileinput);
				
					
					while((s=br.readLine())!=null){
						//125	互联网电视	互联网电视	professionalwords<#>accessory	针对性同义词*<#>重点词*<#>相关名词<#>	2017-11-30
						s=s.toLowerCase();
						String phraseno=s.split("\t")[0];
						String phrase=s.split("\t")[1];
						String phraseroot=s.split("\t")[2];
						String phraselabel=s.split("\t")[3];
						String phrasetype=s.split("\t")[4];
						 if(phraselabel.equals("professionalwords")||phraselabel.equals("generalwords")||phraselabel.equals("generalwords<#>professionalwords")||phraselabel.equals("professionalwords<#>generalwords")){
							String phraseversion=s.split("\t")[5];
							List<String> list=new ArrayList<String>();
							list.add(phraseno);
							list.add(phraseroot);
							list.add(phraselabel);
							list.add(phrasetype);
							list.add(phraseversion);
							if(importantDoc.contains(phrase))
							{
								continue;
							}
							if(phrase.length()>=2)// 单个中文不要
							{
								phrase2infoProfessionalDic.put(phrase, list);
								
							}
							
							//phrase2infoOtherDic.put(phrase, list);
							
						}
							
									
											
					}
					
					System.out.println("phrase2infoProfessionalDic"+ phrase2infoProfessionalDic.size());
					
				}catch (Exception e) {
					
					e.printStackTrace();
				}
				finally
				{
					if(br!=null)
					{
					try {
						br.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
					br=null;
					}
					
					System.out.println("success=========================ptxe");
				}

					
			
				  	
			//System.out.println(phrase2types);
			
			
			Map<String,List<String>> taggingfour=new TreeMap<String,List<String>>();
			BufferedWriter bwscfour=null;
		  
		    BufferedWriter bwscall=null;
		    //OutputStreamWriter  sc=new OutputStreamWriter(new FileOutputStream("Q:\\huawei\\dictionary\\professionalwords_Id_Type_dictionary.csv"),"UTF-8");
		    OutputStreamWriter  scall=new OutputStreamWriter(new FileOutputStream(resultPath+"_tempResults"),"UTF-8");
			
		    
		    bwscall=new BufferedWriter(scall);
			
			Set<String> types=new HashSet<String>();
			int tagquery=0;
			try {
				
				InputStreamReader	read = new InputStreamReader(new FileInputStream(new File(filePath)),"UTF-8"); 
				br=new BufferedReader(read);
				
				String s="";
				int start=0;
				while((s=br.readLine())!=null)
				{
					s=s.toLowerCase();
					String standard=s.split("<&&>")[0];
					Set<String> set=new HashSet<String>();
					if(standard!=null&!standard.equals(""))
					{
											
						
						//从前往后最长切分，
						Map<Integer,String> startPosition2TermsImportantDic=new TreeMap<Integer,String>();
						for(int i=0;i<standard.length();i++)
						{
							int position=i;
							for(int j=i+1;j<=standard.length();j++)
							{
								String phrase=standard.substring(i,j);
								//System.out.println(phrase);
								//
								if(phrase2infoFourDic.containsKey(phrase))
								{
									startPosition2TermsImportantDic.put(i, phrase);
									position=j-1;
								}
								
							}
							i=position;
						}
						
						for(Entry<Integer,String> en:startPosition2TermsImportantDic.entrySet())
						{
							//set.add(en.getValue()+"<&&>"+en.getKey());
							int position=en.getKey();
							String phrase=en.getValue();
													
							List<String> phraseinfo=phrase2infoFourDic.get(phrase);
							
														
							String phraseid=phraseinfo.get(0);
							String phraselabel=phraseinfo.get(2);
							
							
							bwscall.write(standard+"\t"+phrase+"\t"+phraseid+"\t"+position+"\t"+phraselabel+"\n");
							bwscall.flush();
							// 
						}
						Map<String,Integer> sentence2Position=new HashMap<String,Integer>();
						List<String> sentenceWithoutTaggingByImportantDic=new ArrayList<String>();
						int end=0;
						for(Entry<Integer,String> en:startPosition2TermsImportantDic.entrySet())
						{
							int startposition=en.getKey();
							String content=en.getValue();
							int endposition=startposition+content.length();
							/*if(startposition>standard.length())
							{
								System.out.println(standard+"\t"+end+"\t"+ startposition);
							}*/
							
							String sen=standard.substring(end,startposition);
							/*if(standard.equals("荣耀4a电信4g版支持红外吗"))
							{
								System.out.println(standard+"\t"+sen+"\t"+startPosition2TermsImportantDic);
							}*/
							//System.out.println(sen);
							if(sen!=null&&!sen.equals(""))
							{
								sentence2Position.put(sen, end);
								sentenceWithoutTaggingByImportantDic.add(sen);
								
							}
							end=endposition;
							
							// 
						}
						
						String sen=standard.substring(end,standard.length());
						//System.out.println(sen);
						if(sen!=null&&!sen.equals(""))
						{
							sentenceWithoutTaggingByImportantDic.add(sen);
							sentence2Position.put(sen, end);
						}
						
					
						if(sentenceWithoutTaggingByImportantDic.size()>0)
						{
							for(String sentence: sentenceWithoutTaggingByImportantDic)
							{
								/*if(!sentence2Position.containsKey(sentence))
								{
									System.out.println(standard+"\t"+ sentence);
								}*/
								int senposition=sentence2Position.get(sentence);
								Map<Integer,String> startPosition2TermsProfessionalDic=new HashMap<Integer,String>();
								for(int i=0;i<sentence.length();i++)
								{
									int position=i;
									for(int j=i+1;j<=sentence.length();j++)
									{
										String phrase=sentence.substring(i,j);
										//System.out.println(phrase);
										//
										if(phrase2infoProfessionalDic.containsKey(phrase))
										{
											startPosition2TermsProfessionalDic.put(senposition+i, phrase);
											position=j-1;
										}
										/*else
										{
											i=j;
										}*/
									}
									i=position;
								}
								
								
								
								
								for(Entry<Integer,String> en:startPosition2TermsProfessionalDic.entrySet())
								{
									int position=en.getKey();
									String phrase=en.getValue();
									
									List<String> phraseinfo=phrase2infoProfessionalDic.get(phrase);
																	
									
									String phraseid=phraseinfo.get(0);
									String phraselabel=phraseinfo.get(2);
									
									
									bwscall.write(standard+"\t"+phrase+"\t"+phraseid+"\t"+position+"\t"+phraselabel+"\n");
									bwscall.flush();
								}
							}
						}
						
									
						
						
					}
					
					
					
					
										
					
				}
				
				String tempPath=resultPath+"_tempResults";
				writeFinalTagsNoOverLap(tempPath,resultPath);
				
			
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			finally
			{
				if(br!=null)
				{
				try {
					br.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				br=null;
				}
				
				System.out.println("success=========================ptxe");
			}

					    
			

}
	
	

	

	
	
	public static void writeFinalTagsNoOverLap(String tempPath ,String resultPath) throws Exception, IOException
	{
		
		FileReader fileinput = null;
		BufferedReader br=null;
	Map<String,List<EnScore>> qidqueryterm2Tags=new HashMap<String,List<EnScore>>();
	
				
		try {
			fileinput = new FileReader(tempPath);
			br=new BufferedReader(fileinput);
			String s=null;
			
			while((s=br.readLine())!=null){
				s=s.toLowerCase();
			//969	如何防止别人盗用我的 Wi-Fi？	盗用	3933	6	professionalwords
				String query=s.split("\t")[0];
				String pharse=s.split("\t")[1];
				String pharseid=s.split("\t")[2];
				double position=Double.parseDouble(s.split("\t")[3]);
				String label=s.split("\t")[4];
				String labelno="general";
							
				
				
				
				if(label.contains("symptom"))
				{
					labelno="symptom";
				}
				
				if(label.equals("a")||label.contains("a<#>")||label.contains("<#>a"))
				{
					labelno="application";
				}
				
				if(label.contains("product"))
				{
					labelno="product";
				}
				/*if(label.equals("professionalwords"))
				{
					labelno="o";
				}*/
				List<EnScore> list=new ArrayList<EnScore>();
				if(qidqueryterm2Tags.containsKey(query))
				{
					list=qidqueryterm2Tags.get(query);
				}
				
				EnScore en=new EnScore();
				en.setE(pharse+"_"+labelno);
				en.setScore(1.0/(1.0+position));
				en.setId(position+"");
				list.add(en);
				qidqueryterm2Tags.put(query, list);
				
			}
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		finally
		{
			if(br!=null)
			{
			try {
				br.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			br=null;
			}
			
			System.out.println("success=========================ptxe");
		}
		  	
	//System.out.println(phrase2types);
	
	
		
		BufferedWriter bwscfour=null;
	    //OutputStreamWriter  sc=new OutputStreamWriter(new FileOutputStream("Q:\\huawei\\dictionary\\professionalwords_Id_Type_dictionary.csv"),"UTF-8");
	    OutputStreamWriter  scfour=new OutputStreamWriter(new FileOutputStream(resultPath),"UTF-8");
		
	    
	    bwscfour=new BufferedWriter(scfour);
	    
	    for(Entry<String,List<EnScore>> entry: qidqueryterm2Tags.entrySet())
	    {
	    	String qidqueryterm=entry.getKey();
	    	List<EnScore> list=entry.getValue();
	    	Collections.sort(list);
	    	StringBuffer sb=new StringBuffer();
	    	for(EnScore en: list)
	    	{
	    		if(sb.length()==0)
	    		{
	    			sb.append("<"+en.getE()+"&"+(int)Double.parseDouble(en.getId())+">");
	    		}
	    		else
	    		{
	    			sb.append("\t").append("<"+en.getE()+"&"+(int)Double.parseDouble(en.getId())+">");
	    		}
	    	}
	    		    	 
	    
	    	 bwscfour.write(qidqueryterm+"\t"+sb.toString()+"\n");
	    	 bwscfour.flush();
	    }
		
		
	}
	
	public void runAnnotation(String[] dictionaryPaths, String outputPath, String inputPath)
	{
		try
		{
			questiontagbyAlldictionaryNoOverlap(dictionaryPaths,outputPath,inputPath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	/*
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String []dictionaryPaths=new String[5];
		dictionaryPaths[0]="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/generalwords.v2.csv";
		dictionaryPaths[1]="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/professionalwords.v2.csv";
		dictionaryPaths[2]="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/product.v3.txt_update_update";
		dictionaryPaths[3]="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/symptom.v3.txt_update_update";
		dictionaryPaths[4]="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/A.v3.txt_update_update";
		String outputPath="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/entitySearchLabelResults";
		String inputPath="/Users/alexaulabaugh/Desktop/ResearchFA17/2017f-entitylucene/NERDocumentAnnotator/src/ner/annotation/extraction/huawei_tagging/entitySearch/StandardQuestion_ExtendQuestion.txt";
		questiontagbyAlldictionaryNoOverlap(dictionaryPaths,outputPath,inputPath);
	
		
	}
	*/

}

class EnScore implements Serializable, Comparable<EnScore>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -822718322651956415L;
	/**
	 * 
	 */

	public EnScore(String e) {
		super();
		this.e = e;
	
	}
	public boolean isMatch() {
		return match;
	}
	public void setMatch(boolean match) {
		this.match = match;
	}
	public boolean match=false;
	public String id;
	public String getId() {
		return id;
	}
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String docId;
	
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String e;
	public String category;
	
	public String getE() {
		return e;
	}
	public EnScore() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public EnScore(String e, double score) {
		super();
		this.e = e;
		this.score = score;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e == null) ? 0 : e.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnScore other = (EnScore) obj;
		if (e == null) {
			if (other.e != null)
				return false;
		} else if (!e.equals(other.e))
			return false;
		
		return true;
	}
	
	
	@Override
	public String toString() {
		return "EScore [match=" + match + ", e=" + e + ", score=" + score + "]";
	}
	public void setE(String e) {
		this.e = e;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double score;
	public int compareTo(EnScore o) {
		// TODO Auto-generated method stub
		
		EnScore t=o;
		 return Double.compare(t.getScore(),this.getScore());
	}
	
	
	
}
