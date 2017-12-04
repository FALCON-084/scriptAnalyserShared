package com.ust.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.ust.diagram.manipulator.DiagramManipulator;
import com.ust.model.CodeBlockModel;
import com.ust.model.ResponseModel;

@Component
public class ScriptParser {

	@Autowired
	DiagramManipulator diagramManipulator;

	public ResponseModel parse(MultipartFile inputFile) throws Exception
	{
		ResponseModel respModel = new ResponseModel();
		List<CodeBlockModel> codeBlockList = new ArrayList<CodeBlockModel>();
		//Try starts

		//			Select select = (Select) CCJSqlParserUtil.parse("");
		//			PlainSelect ps = (PlainSelect) select.getSelectBody();
		//			System.out.println("Try starts");
		//			System.out.println(ps.getWhere().toString());
		//			System.out.println(ps.getSelectItems().get(1).toString());
		//			                // here you have to check what kind of expression it is and execute your actions individually for every expression implementation
		//			AndExpression e = (AndExpression) ps.getWhere();
		//			System.out.println(e.getLeftExpression());  // which is another expression you can drill down
		//			System.out.println("Try ends");


		//Try ends

		List<String> lstFileLines = saveFileToModal(inputFile, respModel);

		ArrayList<String> emptyList = new ArrayList<String>();
		List<ArrayList<String>> insertsList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> updatesList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> createsList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> deletesList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> volatilesList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> mergeUpdatesList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> mergeInsertsList = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> srctableListcopy=  new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> tarTableListcopy=  new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> tempListcopy=  new ArrayList<ArrayList<String>>();
		Map<String, List<ArrayList<String>>> cleanMap = new HashMap<String,List<ArrayList<String>>>();
		Map<Integer,String> cbidLnNum = new HashMap<Integer,String>();
		Map<Integer,List<String>> cbIDQuery = new HashMap<Integer,List<String>>();
		Map<Integer,String> cbidAction = new HashMap<Integer,String>();
		Map<Integer,List<String>> srcTableList = new HashMap<Integer,List<String>>();
		Map<Integer,List<String>> tarTableList = new HashMap<Integer,List<String>>();
		Map<Integer,List<String>> stAloneTableList = new HashMap<Integer,List<String>>();
		Map<Integer,String> diGrapgList = new HashMap<Integer,String>();

		int i =0;
		String DescripTemp =new String();
		for(i=0;i<100;i++){
			DescripTemp+=  lstFileLines.get(i); 
		}
		int strtInd =0;
		int endInd =0;
		strtInd = DescripTemp.toLowerCase().indexOf("description")+11;
		endInd = DescripTemp.toLowerCase().indexOf("source tables");
		DescripTemp =DescripTemp.substring(strtInd,endInd).replaceAll("\t", " ").replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", "").replaceAll("#", "").replaceAll(":", "");
		System.out.println("destemp"+DescripTemp);
		respModel.setDescription(DescripTemp);
		removeCommentsCommands(lstFileLines);
		extractSqlStatements(lstFileLines, emptyList, insertsList, cbidLnNum, cbIDQuery, cbidAction, srcTableList,
				tarTableList, stAloneTableList, diGrapgList);
		respModel.setVolatilesList(extractVolatileTables(cbidAction,stAloneTableList));
		cleanMap.put("I",insertsList);
		cleanMap.put("U",updatesList);
		cleanMap.put("C",createsList);
		cleanMap.put("CV",volatilesList);
		cleanMap.put("D",deletesList);
		cleanMap.put("MI",mergeInsertsList);
		cleanMap.put("MU",mergeUpdatesList);

		setCodeBlock(respModel, codeBlockList, cbidLnNum, cbIDQuery, cbidAction, srcTableList, tarTableList,
				stAloneTableList, diGrapgList);

		List<String> srcTablesPreHash = new ArrayList<String>() ;
		List<String> tarTablesPreHash = new ArrayList<String>() ;
		for(List<String>  srctableListcopy1 : srcTableList.values()) {
			srctableListcopy.add((ArrayList<String>) srctableListcopy1);
			srcTablesPreHash.addAll(srctableListcopy1);
		}
		for(i=0; i <= (srctableListcopy.size()-1) ;i++){
			ArrayList<String> newList = new ArrayList<String>(new HashSet<String>(srctableListcopy.get(i)));
			tempListcopy.add((ArrayList<String>) newList);
		}

		for(List<String>  tarTableListcopy1 : tarTableList.values()) {
			tarTableListcopy.add((ArrayList<String>) tarTableListcopy1);
			tarTablesPreHash.addAll(tarTableListcopy1);
		}
		ArrayList<String> srcTables = new ArrayList<String>(new HashSet<String>(srcTablesPreHash));
		ArrayList<String> tarTables = new ArrayList<String>(new HashSet<String>(tarTablesPreHash));
		respModel.setSrctableList(srcTables);
		respModel.setTartableList(tarTables);
		setMainGraph(respModel, tempListcopy, tarTableListcopy,cbidAction);
		addDescToFileContent(respModel);
		return respModel;

	}
	private void setMainGraph(ResponseModel respModel, List<ArrayList<String>> srctableListcopy,
			List<ArrayList<String>> tarTableListcopy, Map<Integer,String> cbidAction) {
		String completeGraph="digraph prof {rankdir=LR;  ratio = fill; node [style=filled, penwidth=2,  fontsize=8.0, shape=box];";
		String graph ="";

		for(int i=0,j=0; i<srctableListcopy.size() ;i++,j++)
		{
			
			if(srctableListcopy.get(i).contains("")){

				continue;
			}
			else{
				if(tarTableListcopy.get(j).isEmpty()){
					continue;
				}
				else{
					Iterator<String> itr = srctableListcopy.get(i).iterator();
					if(itr.hasNext()){
						String tempGraph = "";
						while(itr.hasNext()){
							if((cbidAction.get(i+1)).equals("C")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=green fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							if((cbidAction.get(i+1)).equals("CV")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=green fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							if((cbidAction.get(i+1)).equals("I")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=blue fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							if((cbidAction.get(i+1)).equals("MI")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=cyan fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							if((cbidAction.get(i+1)).equals("U")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=red fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							if((cbidAction.get(i+1)).equals("MU")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=orange fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							if((cbidAction.get(i+1)).equals("D")){
							tempGraph += itr.next().toString().replace("[", "").replaceAll("]", "").replaceAll(";", "") + " -> " +  tarTableListcopy.get(j).toString().replace("[", "").replaceAll("]", "").replaceAll(";", "")+ " [color=brown fontsize=8.0 penwidth=2 label= \"CBID " + (i+1) + "\"];";}
							
						}
						graph = tempGraph;
					}

				}
			}
			completeGraph += graph;
		}
		completeGraph += "}";
		System.out.println("Complete Graph "  + completeGraph);
		respModel.setCompleteGraph(completeGraph);
	}
	private void extractSqlStatements(List<String> lstFileLines, ArrayList<String> emptyList,
			List<ArrayList<String>> insertsList, Map<Integer, String> cbidLnNum, Map<Integer, List<String>> cbIDQuery,
			Map<Integer, String> cbidAction, Map<Integer, List<String>> srcTableList,
			Map<Integer, List<String>> tarTableList, Map<Integer, List<String>> stAloneTableList,
			Map<Integer, String> diGrapgList) throws Exception {
		int tableCounter=0;
		int queryCounter = 1;
		int cbid = 1;
		int actionCounter=1;
		int digraphCounter=1;
		String lineTemp ="";
		for (int i = 0; i < lstFileLines.size(); i++) {
			lineTemp = lstFileLines.get(i);
			if(lineTemp.contains("update")){
				lineTemp = lineTemp.replaceAll("update","UPDATE");
				lstFileLines.remove(i);
				lstFileLines.add(i, lineTemp);
			}
			
			if (lineTemp.startsWith("/*") | (lineTemp.startsWith("STEP")) | lineTemp.startsWith("--") | lineTemp.contains("#") | lineTemp.startsWith(".") | lineTemp.startsWith("echo") | lineTemp.startsWith("exit") | lineTemp.contains("EOF")) 
				continue;
			else 
			{	//default is merge update else ,we check for merge _insert
				ArrayList<String> tmpList = new ArrayList<String>();
				String tmpAction = null;
				String lnNum = "";
				if (lineTemp.contains(("MERGE"))) {
					tmpAction = "MU" ;
					lnNum = ((i +1) + " - ");
					for (int j = i; j < lstFileLines.size(); j++) {
						if (lstFileLines.get(j).startsWith("/*") | (lstFileLines.get(j).startsWith("STEP")) | lstFileLines.get(j).startsWith("--") | lstFileLines.get(j).startsWith("#") | lstFileLines.get(j).startsWith(".") | lstFileLines.get(j).startsWith("echo") | lstFileLines.get(j).contains("exit") | lstFileLines.get(j).contains("EOF")){
							continue;
						}
						else
						{
							String Mtemp = lstFileLines.get(j).replaceAll("\t", " ").replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", "");
							if(Mtemp.contains("INSERT")){
								tmpAction = "MI" ;
							}
							tmpList.add(Mtemp);
						}

						if (lstFileLines.get(j).contains(";")) {
							i=j;
							lnNum = (lnNum + (i+1));
							break;
						}
					}
				}
				else if (lineTemp.contains(("INSERT"))) {
					tmpAction = "I" ;
					lnNum = ((i +1) + " - ");
					for (int j = i; j < lstFileLines.size(); j++) {
						if (lstFileLines.get(j).startsWith("/*") | (lstFileLines.get(j).startsWith("STEP")) | lstFileLines.get(j).startsWith("--") | lstFileLines.get(j).startsWith("#") | lstFileLines.get(j).startsWith(".") | lstFileLines.get(j).startsWith("echo") | lstFileLines.get(j).contains("exit") | lstFileLines.get(j).contains("EOF")){
							continue;
						}
						else
						{
							tmpList.add(lstFileLines.get(j).replaceAll("\t", " ").replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", ""));
						}

						if (lstFileLines.get(j).contains(";")) {
							i=j;
							lnNum = (lnNum + (i+1));
							break;
						}
					}
				}
				else if (lineTemp.contains(("UPDATE"))) {
					tmpAction = "U" ;
					lnNum = ((i +1) + " - ");
					for (int j = i; j < lstFileLines.size(); j++) {
						if (lstFileLines.get(j).startsWith("/*") | (lstFileLines.get(j).startsWith("STEP")) | lstFileLines.get(j).startsWith("--") | lstFileLines.get(j).startsWith("#") | lstFileLines.get(j).startsWith(".") | lstFileLines.get(j).startsWith("echo") | lstFileLines.get(j).contains("exit") | lstFileLines.get(j).contains("EOF")){
							continue;
						}
						else
						{
							tmpList.add(lstFileLines.get(j).replaceAll("\t", " ").replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", ""));
						}
						if (lstFileLines.get(j).contains(";")) {
							i=j;
							lnNum = (lnNum + (i+1));
							break;
						}
					}
				}

				else if (lineTemp.contains(("CREATE"))) {
					tmpAction = "C" ;
					lnNum = ((i +1) + " - ");
					for (int j = i; j < lstFileLines.size(); j++) {
						if (lstFileLines.get(j).startsWith("/*") | (lstFileLines.get(j).startsWith("STEP")) | lstFileLines.get(j).startsWith("--") | lstFileLines.get(j).startsWith("#") | lstFileLines.get(j).startsWith(".") | lstFileLines.get(j).startsWith("echo") | lstFileLines.get(j).contains("exit") | lstFileLines.get(j).contains("EOF")){
							continue;
						}
						else
						{
							String Mtemp = lstFileLines.get(j).replaceAll("\t", " ").replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", "");
							if(Mtemp.contains("VOLATILE")){
								tmpAction = "CV" ;
							}
							tmpList.add(Mtemp);
						}
						if (lstFileLines.get(j).contains(";")) {
							i=j;
							lnNum = (lnNum + (i+1));
							break;
						}
					}
				}	
				else if (lineTemp.contains(("DELETE"))) {

					tmpAction = "D" ;
					lnNum = ((i +1) + " - ");

					for (int j = i; j < lstFileLines.size(); j++) {
						// System.out.println(str.get(j));
						if (lstFileLines.get(j).startsWith("/*") | (lstFileLines.get(j).startsWith("STEP")) | lstFileLines.get(j).startsWith("--") | lstFileLines.get(j).startsWith("#") | lstFileLines.get(j).startsWith(".") | lstFileLines.get(j).startsWith("echo") | lstFileLines.get(j).contains("exit") | lstFileLines.get(j).contains("EOF")){
							continue;
						}
						else
						{
							tmpList.add(lstFileLines.get(j).replaceAll("\t", " ").replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", ""));
						}
						if (lstFileLines.get(j).contains(";")) {
							i=j;
							lnNum = (lnNum + (i+1));
							break;
						}
					}
				}

				else{
					continue;
				}

				sanitizeQuery(tmpList);

				++tableCounter;
				switch(tmpAction){
				case "I" :
				case "U":
				case "C":
				case "CV":
				case "MI":
				case "MU":
					extractCatTblsFromCrUpd(emptyList, insertsList, srcTableList, tarTableList, stAloneTableList,
							tableCounter, tmpList);
					break;
				case "D": 
					extractTablesFromDelete(emptyList, srcTableList, tarTableList, stAloneTableList, tableCounter,
							tmpList);
					break;
				}

				cbidLnNum.put(cbid++, lnNum);
				cbIDQuery.put(queryCounter++, tmpList);
				cbidAction.put(actionCounter++,tmpAction);

				String tmpFullQuery ="";
				for(int r=0; r <tmpList.size();r++){
					if(tmpList.isEmpty())
						continue;
					else
						tmpFullQuery = tmpFullQuery + ((tmpList.get(r).toString()).replaceAll("\\s{2,}"," ").replaceAll("[\u0000-\u001f]", "")).replaceAll("\\{", "").replaceAll("\\}", "") + " ";
				}
				String graph=diagramManipulator.getGraph(tmpFullQuery,srcTableList.get(cbid-1),tarTableList.get(cbid-1),stAloneTableList.get(cbid-1),cbidAction.get(actionCounter-1));
				diGrapgList.put(digraphCounter++,graph);
			}

		}
	}
	private void sanitizeQuery(ArrayList<String> tmpList) {
		tmpList.removeAll(Arrays.asList("",null));
	}
	/**
	 * @param respModel
	 * @param codeBlockList
	 * @param cbidLnNum
	 * @param cbIDQuery
	 * @param cbidAction
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param diGrapgList
	 */
	private void setCodeBlock(ResponseModel respModel, List<CodeBlockModel> codeBlockList,
			Map<Integer, String> cbidLnNum, Map<Integer, List<String>> cbIDQuery, Map<Integer, String> cbidAction,
			Map<Integer, List<String>> srcTableList, Map<Integer, List<String>> tarTableList,
			Map<Integer, List<String>> stAloneTableList, Map<Integer, String> diGrapgList) {
		for (Entry<Integer, List<String>> entry : cbIDQuery.entrySet()) {
			CodeBlockModel modelObj = new CodeBlockModel();
			//  System.out.println(entry.getKey() + " " + entry.getValue());
			modelObj.setCbid(entry.getKey());
			modelObj.setQuery(entry.getValue());
			//	modelObj.setGraph("digraph prof { rankdir=LR; ratio = fill; node [style=filled, shape=box];s -> r [color=red label=\"Inner Join\"]r -> h1 [color=red label=\"Inner Join\"]r -> h2 [color=red label=\"Inner Join\"]r -> h3 [color=red label=\"Inner Join\"]}");
			modelObj.setGraph(diGrapgList.get(entry.getKey()));
			modelObj.setLineno(cbidLnNum.get(entry.getKey()));
			modelObj.setAction(cbidAction.get(entry.getKey()));
			modelObj.setDbSrcTables(srcTableList.get(entry.getKey()));	
			modelObj.setDbTargetTables(tarTableList.get(entry.getKey()));	
			modelObj.setDbStAloneTables(stAloneTableList.get(entry.getKey()));
			codeBlockList.add(modelObj);
		}
		respModel.setCodeBlocks(codeBlockList);
	}
	/**
	 * @param respModel
	 * @param codeBlockList
	 * @param cbidLnNum
	 * @param cbIDQuery
	 * @param cbidAction
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param diGrapgList
	 */
	private List<String> extractVolatileTables(Map<Integer, String> cbidAction,	Map<Integer, List<String>> stAloneTableList) {
		List<String> volatilesList = new ArrayList<String>();
		for(int itr=0;itr<stAloneTableList.size();itr++){
			if(cbidAction.get(itr) == "CV"){
				volatilesList.addAll(stAloneTableList.get(itr));
			}
		}
		System.out.println(volatilesList);
		return volatilesList;
		
	}
	/**
	 * @param emptyList
	 * @param insertsList
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param tableCounter
	 * @param tmpList
	 */
	private void extractCatTblsFromCrUpd(ArrayList<String> emptyList, List<ArrayList<String>> insertsList,
			Map<Integer, List<String>> srcTableList, Map<Integer, List<String>> tarTableList,
			Map<Integer, List<String>> stAloneTableList, int tableCounter, ArrayList<String> tmpList) {
		insertsList.add(tmpList);
		List<String> tarTemp = new ArrayList<String>();
		ArrayList<String> srcList = new ArrayList<String>();
		//merge-tag is 1 when a query is detected as merge .. then insert operation doesnt take place
		int mergeTag =0;
		for(int itr=0;itr<tmpList.size();itr++){
			//			System.out.println("extractCat "+itr+" "+tarTableList);
			//			System.out.println("extractCat "+"has query"+tmpList.get(itr));
			if(tmpList.get(itr).contains("MERGE")){
				populateMergeQuery(emptyList, srcTableList, tarTableList, stAloneTableList,
						tableCounter, tmpList, tarTemp, itr);
				mergeTag =1;
			}
			else if(tmpList.get(itr).contains("INSERT") && mergeTag ==0){
				populateInsertQuery(emptyList, srcTableList, tarTableList, stAloneTableList,
						tableCounter, tmpList, tarTemp, itr);
			}
			else if(tmpList.get(itr).contains("UPDATE")){
				populateUpdateQuery(emptyList, srcTableList, tarTableList, stAloneTableList,
						tableCounter, tmpList, tarTemp, itr);
			}
			else if(tmpList.get(itr).contains("CREATE")){ 
				extractCatTblsFromCreate(emptyList, srcTableList, tarTableList, stAloneTableList, tableCounter,
						tmpList);
			}
			else
				continue;
		}
		for(int itr=0;itr<tmpList.size();itr++){
			if(tmpList.get(itr).contains("_FROM") || tmpList.get(itr).contains("FROM_")){
				continue;
			}
			if(tmpList.get(itr).contains("FROM")){
				if(tmpList.get(itr).contains("SUBSTR"))
					continue;
				else{
					List<String> tmpFrom = new ArrayList<String>();
					String[] fromtxt = tmpList.get(itr).trim().toString().split(" ");
					if(fromtxt.length==1 || (fromtxt[(fromtxt.length-1)].equalsIgnoreCase("FROM"))){
						String[] fromtxt1 = tmpList.get(itr+1).trim().toString().split(" ");
						if(fromtxt1[0].equalsIgnoreCase("(")||fromtxt1[0].contains("(")){
							itr +=1;
							continue;
						}
						else {
							itr +=1;
							srcList.add(fromtxt1[0].replace(";", ""));
							continue;
						}
					}
					else{
						for(int k=0;k<fromtxt.length;k++){
							if(fromtxt[k].isEmpty()){
								continue;
							}
							else
							{
								tmpFrom.add(fromtxt[k]);
							}
						}
						
						while(tmpFrom.contains("FROM") ){
							if((tmpFrom.get(tmpFrom.indexOf("FROM")+1).equalsIgnoreCase("SUBSTR")))
								break;
							else if((tmpFrom.get(tmpFrom.indexOf("FROM")+1).equalsIgnoreCase("(")) || (tmpFrom.get(tmpFrom.indexOf("FROM")+1).contains("("))){
								if(tmpFrom.subList(tmpFrom.indexOf("FROM")+1, tmpFrom.size()).contains("FROM")){
									tmpFrom=tmpFrom.subList(tmpFrom.indexOf("FROM")+1, tmpFrom.size());
									System.out.println("Inside Else if");
									System.out.println("FROM + 1 " + tmpFrom.get(tmpFrom.indexOf("FROM")+1) );
								}
								else
								{
									break;
								}

							}
							else
							{
								srcList.add(tmpFrom.get(tmpFrom.indexOf("FROM")+1).replace(";", ""));
								break;
							}
						}

						/*for(int s=tmpFrom.indexOf("FROM")+1; s<tmpFrom.indexOf("FROM")+2;)
						{
							if(tmpFrom.contains("SUBSTR"))
								break;
							else if(tmpFrom.get(s).contains("(")){

							}
								srcList.add(tmpFrom.get(s).replace(";", ""));
							break;
						}*/
					}
				}
			}
			if(tmpList.get(itr).contains("JOIN")){
				List<String> tmpFrom = new ArrayList<String>();
				String[] fromtxt = tmpList.get(itr).trim().toString().split(" ");
				if(fromtxt.length==1 || (fromtxt[(fromtxt.length-1)].equalsIgnoreCase("JOIN"))){
					String[] fromtxt1 = tmpList.get(itr+1).trim().toString().split(" ");
					if(fromtxt1[0].equalsIgnoreCase("(")||fromtxt1[0].contains("(")){
						itr +=1;
						continue;
					}
					else {
						itr +=1;
						srcList.add(fromtxt1[0].replace(";", ""));
						continue;
					}
				}
				else{
					for(int k=0;k<fromtxt.length;k++){
						if(fromtxt[k].isEmpty()){
							continue;
						}
						else
						{
							tmpFrom.add(fromtxt[k]);
						}
					}
					for(int s=tmpFrom.indexOf("JOIN")+1; s<tmpFrom.indexOf("JOIN")+2;s++)
					{
						if(tmpFrom.get(s).contains("("))
							break;
						else
						{
							srcList.add(tmpFrom.get(s).replace(";", ""));
							break;
						}
					}
				}
			}
			if(tmpList.get(itr).contains("USING")){
				List<String> tmpFrom = new ArrayList<String>();
				String[] fromtxt = tmpList.get(itr).trim().toString().split(" ");
				if(fromtxt.length==1 || (fromtxt[(fromtxt.length-1)].equalsIgnoreCase("USING"))){
					String[] fromtxt1 = tmpList.get(itr+1).trim().toString().split(" ");
					if(fromtxt1[0].equalsIgnoreCase("(")||fromtxt1[0].contains("(")){
						itr +=1;
						continue;
					}
					else {
						itr +=1;
						srcList.add(fromtxt1[0].replace(";", ""));
						continue;
					}
				}
				else{
					for(int k=0;k<fromtxt.length;k++){
						if(fromtxt[k].isEmpty()){
							continue;
						}
						else
						{
							tmpFrom.add(fromtxt[k]);
						}
					}
					for(int s=tmpFrom.indexOf("USING")+1; s<tmpFrom.indexOf("USING")+2;s++)
					{
						if(tmpFrom.get(s).contains("("))
							break;
						else
						{
							srcList.add(tmpFrom.get(s).replace(";", ""));
							break;
						}
					}
				}
			}

		}

		srcTableList.put(tableCounter, srcList);
	}
	/**
	 * @param emptyList
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param tableCounter
	 * @param tmpList
	 */
	private void extractTablesFromDelete(ArrayList<String> emptyList, Map<Integer, List<String>> srcTableList,
			Map<Integer, List<String>> tarTableList, Map<Integer, List<String>> stAloneTableList, int tableCounter,
			ArrayList<String> tmpList) {
		List<String> srcTempD = new ArrayList<String>();
		for(int itr=0;itr<tmpList.size();itr++){

			if(tmpList.get(itr).contains("DELETE")){
				String[] tmpTarTables = tmpList.get(itr).split(" ");

				ArrayList<String> tarList = new ArrayList<String>();

				for(int k=0;k<tmpTarTables.length;k++){
					if(tmpTarTables[k].isEmpty()){
						continue;
					}
					else
					{
						tarList.add(tmpTarTables[k]);
					}
				}
				for(int y = tarList.indexOf("FROM")+1; y<tarList.size();y++)
				{
					srcTempD.add(tarList.get(y));
					break;
				}
				stAloneTableList.put(tableCounter,srcTempD);
				tarTableList.put(tableCounter,emptyList);
				srcTableList.put(tableCounter, emptyList);
			}
		}
	}
	/**
	 * @param emptyList
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param tableCounter
	 * @param tmpList
	 */
	private void extractCatTblsFromCreate(ArrayList<String> emptyList, Map<Integer, List<String>> srcTableList,
			Map<Integer, List<String>> tarTableList, Map<Integer, List<String>> stAloneTableList, int tableCounter,
			ArrayList<String> tmpList) {
		List<String> srcTempC = new ArrayList<String>();
			for(int itr=0;itr<tmpList.size();itr++){

			if(tmpList.get(itr).contains("CREATE")){
				String[] tmpTarTables = tmpList.get(itr).split(" ");

				ArrayList<String> tarList = new ArrayList<String>();

				for(int k=0;k<tmpTarTables.length;k++){
					if(tmpTarTables[k].isEmpty()){
						continue;
					}
					else
					{
						tarList.add(tmpTarTables[k]);
					}
				}
				for(int y = tarList.indexOf("TABLE")+1; y<tarList.size();y++)
				{
					System.out.println("Target " +tarList.get(y).toString());
					srcTempC.add(tarList.get(y));
				}

				while(srcTempC.size()>1){
					srcTempC.remove(1);
				}
				/*OR
				List<String> srcTempCFirst = new ArrayList<String>();
				srcTempCFirst.add(srcTempC.get(0));
				stAloneTableList.put(tableCounter,srcTempCFirst);*/
				//replace the below line with the above three lines
				stAloneTableList.put(tableCounter,srcTempC);
				tarTableList.put(tableCounter,emptyList);
				srcTableList.put(tableCounter, emptyList);
			}
		}
	}
	/**
	 * @param emptyList
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param tableCounter
	 * @param tmpList
	 * @param tarTemp
	 * @param itr
	 */
	private void populateUpdateQuery(ArrayList<String> emptyList, Map<Integer, List<String>> srcTableList,
			Map<Integer, List<String>> tarTableList, Map<Integer, List<String>> stAloneTableList, int tableCounter,
			ArrayList<String> tmpList, List<String> tarTemp, int itr) {
		String[] tmpTarTables = tmpList.get(itr).split(" ");
		ArrayList<String> tarList = new ArrayList<String>();
		for(int k=0;k<tmpTarTables.length;k++){
			if(tmpTarTables[k].isEmpty()){
				continue;
			}
			else
			{
				tarList.add(tmpTarTables[k]);
			}
		}
		for(int y = tarList.indexOf("UPDATE")+1; y<tarList.size();y++)
		{
			if(tarList.get(y).equalsIgnoreCase("TGT")){
				for(int itr1=0;itr1<tmpList.size();itr1++){
					if(tmpList.get(itr1).contains("FROM")){
						if(tmpList.get(itr1).contains("SUBSTR"))
							continue;
						else{
							List<String> tmpFrom = new ArrayList<String>();
							String[] fromtxt = tmpList.get(itr1).trim().toString().split(" ");
							if(fromtxt.length==1 || (fromtxt[(fromtxt.length-1)].equalsIgnoreCase("FROM"))){
								String[] fromtxt1 = tmpList.get(itr1+1).trim().toString().split(" ");
								if(fromtxt1[0].equalsIgnoreCase("(")||fromtxt1[0].contains("(")){
									itr1 +=1;
									continue;
								}
								else {
									itr1 +=1;
									if(fromtxt1[0].contains("$"))
										fromtxt1[0] = fromtxt1[0].substring(fromtxt1[0].indexOf(".")+1);
									tarTemp.add(fromtxt1[0].replace(";", ""));
									break;
								}
							}
							else{
								for(int k=0;k<fromtxt.length;k++){
									if(fromtxt[k].isEmpty()){
										continue;
									}
									else
									{
										tmpFrom.add(fromtxt[k]);
									}
								}
								while(tmpFrom.contains("FROM")){
									if((tmpFrom.get(tmpFrom.indexOf("FROM")+1).equalsIgnoreCase("SUBSTR")))
										break;
									else if((tmpFrom.get(tmpFrom.indexOf("FROM")+1).equalsIgnoreCase("("))){
										if(tmpFrom.subList(tmpFrom.indexOf("FROM")+1, tmpFrom.size()).contains("FROM")){
											tmpFrom=tmpFrom.subList(tmpFrom.indexOf("FROM")+1, tmpFrom.size());
											System.out.println("Inside Else if");
											System.out.println("FROM + 1 " + tmpFrom.get(tmpFrom.indexOf("FROM")+1) );
										}
										else
										{
											break;
										}

									}
									else
									{
										String tmpFromname = null;
										if(tmpFrom.get(tmpFrom.indexOf("FROM")+1).contains("$"))
										{
											tmpFromname = (tmpFrom.get(tmpFrom.indexOf("FROM")+1)).substring(tmpFrom.get(tmpFrom.indexOf("FROM")+1).indexOf(".")+1);
										}
										System.out.println(tmpFromname);
										System.out.println(tarTemp);
										if(tmpFromname != null){
										tarTemp.add(tmpFromname.replace(";", ""));
										}
										break;
										
									}
								}

							}
						}
					}
				}
			}
			else
				tarTemp.add(tarList.get(y));
			break;
		}
		tarTableList.put(tableCounter,tarTemp);
		srcTableList.put(tableCounter, emptyList);
		stAloneTableList.put(tableCounter,emptyList);
		System.out.println("pop update query "+tarTableList);
	}


	/**
	 * @param emptyList
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param tableCounter
	 * @param tmpList
	 * @param tarTemp
	 * @param itr
	 */
	private void populateMergeQuery(ArrayList<String> emptyList, Map<Integer, List<String>> srcTableList,
			Map<Integer, List<String>> tarTableList, Map<Integer, List<String>> stAloneTableList, int tableCounter,
			ArrayList<String> tmpList, List<String> tarTemp, int itr) {
		String[] tmpTarTables = tmpList.get(itr).split(" ");

		ArrayList<String> tarList = new ArrayList<String>();

		for(int k=0;k<tmpTarTables.length;k++){
			if(tmpTarTables[k].isEmpty()){
				continue;
			}
			else
			{
				tarList.add(tmpTarTables[k]);
			}
		}

		int tIndex = tarList.indexOf("INTO")+1;

		if (((tarList.size()-1) >= tarList.indexOf("INTO")+2)){
			for(int y = tarList.indexOf("INTO")+1; y<tarList.indexOf("INTO")+2;y++)
			{
				if(tarList.get(y).contains(" ") || tarList.get(y).endsWith(" ") || tarList.get(y).equalsIgnoreCase(" ")){
					break;
				}
				else{
					tarTemp.add(tarList.get(y).replace("(", ""));
				}
			}
		}
		else
		{
			if (!(tarList.get(tIndex).contains(" ")) || !(tarList.get(tIndex).endsWith(" ")) || !(tarList.get(tIndex).equalsIgnoreCase(" "))){

				tarTemp.add(tarList.get(tIndex).replace("(", ""));
			}
		}
		if(tarTemp.size()>1){
			tarTemp.remove(1);
		}
		tarTableList.put(tableCounter,tarTemp);
		srcTableList.put(tableCounter, emptyList);
		stAloneTableList.put(tableCounter,emptyList);
		System.out.println("pop merge query "+tarTableList);
	}
	/**
	 * @param emptyList
	 * @param srcTableList
	 * @param tarTableList
	 * @param stAloneTableList
	 * @param tableCounter
	 * @param tmpList
	 * @param tarTemp
	 * @param itr
	 */
	private void populateInsertQuery(ArrayList<String> emptyList, Map<Integer, List<String>> srcTableList,
			Map<Integer, List<String>> tarTableList, Map<Integer, List<String>> stAloneTableList, int tableCounter,
			ArrayList<String> tmpList, List<String> tarTemp, int itr) {
		String[] tmpTarTables = tmpList.get(itr).split(" ");
		if(tmpList.get(itr).trim().toLowerCase().equals("insert into")){
			String checkedString =  tmpList.get(itr)+tmpList.get(itr+1);
		    tmpTarTables =checkedString.split(" ");
		}
		

		ArrayList<String> tarList = new ArrayList<String>();

		for(int k=0;k<tmpTarTables.length;k++){
			if(tmpTarTables[k].isEmpty()){
				continue;
			}
			else
			{
				tarList.add(tmpTarTables[k]);
			}
		}
		int tIndex = tarList.indexOf("INTO")+1;

		if (((tarList.size()-1) >= tarList.indexOf("INTO")+2)){
			for(int y = tarList.indexOf("INTO")+1; y<tarList.indexOf("INTO")+2;y++)
			{
				if(tarList.get(y).contains(" ") || tarList.get(y).endsWith(" ") || tarList.get(y).equalsIgnoreCase(" ")){
					break;
				}
				else{
					tarTemp.add(tarList.get(y).replace("(", ""));
					break;
				}
			}
		}
		else
		{
			
			if (!(tarList.get(tIndex).contains(" ")) || !(tarList.get(tIndex).endsWith(" ")) || !(tarList.get(tIndex).equalsIgnoreCase(" "))){
				tarTemp.add(tarList.get(tIndex).replace("(", ""));
			}
		}

		tarTableList.put(tableCounter,tarTemp);
		srcTableList.put(tableCounter, emptyList);
		stAloneTableList.put(tableCounter,emptyList);
		System.out.println("pop insert query "+tarTableList);
	}
	/**
	 * @param inputFile
	 * @param respModel
	 * @return
	 * @throws IOException
	 */
	/**
	 * @param lstFileLines
	 */
	private void removeCommentsCommands(List<String> lstFileLines) {
		for(int fileLinesIterator=0; fileLinesIterator < lstFileLines.size(); fileLinesIterator++){

			if(lstFileLines.get(fileLinesIterator).trim().startsWith("/*")){
				for(int internalTraverser = fileLinesIterator; internalTraverser< lstFileLines.size();internalTraverser++){
					//	System.out.println(j + " "  + it.get(j));
					if(lstFileLines.get(internalTraverser).trim().endsWith("*/")){
						lstFileLines.remove(internalTraverser);
						lstFileLines.add(internalTraverser, "");
						fileLinesIterator=internalTraverser;
						break;
					}
					else{
						lstFileLines.remove(internalTraverser);
						lstFileLines.add(internalTraverser, "");
					}
				}
			}
			else 
				continue;
		}
		for(int q=0; q < lstFileLines.size(); q++){
			String rcTemp = lstFileLines.get(q).trim();
			if(rcTemp.startsWith("/*")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.isEmpty()){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.endsWith("*/") && (rcTemp.startsWith("*") || rcTemp.startsWith("/*"))){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("STEP")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("--")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("--,")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("#")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith(".")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("echo")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("exit")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(rcTemp.startsWith("eof")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(lstFileLines.get(q).contains("****")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(lstFileLines.get(q).contains("exec ")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			else if(lstFileLines.get(q).contains("Error")){
				lstFileLines.remove(q);
				lstFileLines.add(q, "");
			}
			/*else
				continue;*/


			if(lstFileLines.get(q).contains("--")){
				int indexOfhyphen = lstFileLines.get(q).indexOf("--");
				String templstFileLine = (lstFileLines.get(q).substring(0, (indexOfhyphen-1)));
				lstFileLines.remove(q);
				lstFileLines.add(q, templstFileLine);
			}

			if(lstFileLines.get(q).contains(" /*")){
				int indexOfstart = lstFileLines.get(q).indexOf(" /*");
				String templstFileLine = (lstFileLines.get(q).substring(0, (indexOfstart-1)));
				lstFileLines.remove(q);
				lstFileLines.add(q, templstFileLine);
			}

		}


	}
	private void addDescToFileContent(ResponseModel respModel){
		String temp = respModel.getFileContent();
		temp ="DESCRIPTION :\n"+respModel.getDescription()+"\n"+"ACTUAL CONTENT: \n"+temp;
		respModel.setFileContent(temp);
	}
	private List<String> saveFileToModal(MultipartFile inputFile, ResponseModel respModel) throws IOException {
		byte [] byteArr=inputFile.getBytes();

		InputStream inputStream = new ByteArrayInputStream(byteArr);

		List<String> it = IOUtils.readLines(inputStream);
		StringBuilder data = new StringBuilder();
		it.forEach(line -> data.append(line).append("\n"));

		respModel.setFileContent(data.toString());
		
		return it;


	}

}
