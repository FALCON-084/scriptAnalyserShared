package com.ust.diagram.manipulator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ust.lib.jsqlparser.expression.operators.relational.EqualsTo;
import com.ust.lib.jsqlparser.parser.CCJSqlParserUtil;
import com.ust.lib.jsqlparser.schema.Column;
import com.ust.lib.jsqlparser.schema.Table;
import com.ust.lib.jsqlparser.statement.Statement;
//import com.ust.lib.jsqlparser.statement.create.Create;
import com.ust.lib.jsqlparser.statement.delete.Delete;
import com.ust.lib.jsqlparser.statement.insert.Insert;
import com.ust.lib.jsqlparser.statement.select.Join;
import com.ust.lib.jsqlparser.statement.select.PlainSelect;
import com.ust.lib.jsqlparser.statement.select.Select;
import com.ust.lib.jsqlparser.statement.select.SubSelect;
import com.ust.lib.jsqlparser.statement.update.Update;

@Component
public class DiagramManipulator {


	/*
	 * Code for generating the graph part
	 */
	public String getGraph (String queryStatement, List<String> srcTabList, List<String> tarTableList,List<String> stAloneTabList, String action)  {

		//String graph="digraph prof { rankdir=LR; ratio = fill; node [style=filled, shape=box];"; 
		String graph=""; 
		Statement statement = null;
		queryStatement=queryStatement.toLowerCase();
		queryStatement = sanitizeQuery(queryStatement);

		/*if(queryStatement.trim().substring(0, 6).equals("create"))
		{
			queryStatement=queryStatement.replace(" all", " ");

		}
		 */
		if(queryStatement.trim().substring(0, 6).equals("delete"))
		{
			queryStatement=queryStatement.replace(" all", " ");

		}
		try {
			statement = CCJSqlParserUtil.parse(queryStatement);


			if(statement!=null)
			{
				if(statement instanceof Select)
				{
					graph = parseSelectStatement(graph, statement);
				}
				else if(statement instanceof Update)
				{
					return parseQueryManually(queryStatement, srcTabList, tarTableList,stAloneTabList,action);

				}
				else if(statement instanceof Insert)
				{
					return parseQueryManually(queryStatement, srcTabList, tarTableList,stAloneTabList,action);	
				}
				else if(statement instanceof Delete)
				{
					graph = parseDeleteStatement(graph, statement);
				}
				/*else if(statement instanceof Create)
			{
				return parseQueryManually(queryStatement, srcTabList, tarTableList,stAloneTabList,action);	
				graph = parseCreatetStatement(graph, statement);
			}*/
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("error===>"+queryStatement);
			return parseQueryManually(queryStatement, srcTabList, tarTableList,stAloneTabList,action);	

		}	

		graph="digraph prof { rankdir=LR; ratio = fill; node [style=filled, shape=box];"+graph+"}";
		return graph;
	}

	private String parseQueryManually(String queryStatement, List<String> srcTabList, List<String> tarTableList,List<String> stAloneTabList, String action) {
		List<String> JoinsList = new ArrayList<String>();
		String exGraph = null;
		JoinsList =  getJoinList(queryStatement);
		exGraph = generateGraph(JoinsList,srcTabList,tarTableList,stAloneTabList,action);
		exGraph="digraph prof { rankdir=LR; ratio = fill; node [style=filled, shape=box];"+exGraph+"}";
		return exGraph;
	}

	/*private String parseCreatetStatement(String graph, Statement statement) {

		Create select = (Create) statement;

		String createTable=(select.getTable().getName()).toUpperCase();
		if(select.getWhere()!=null)
		{
			if(select.getWhere() instanceof EqualsTo){
				//Inside Instance of Delete: select.getWhere()!=null && (select.getWhere() instanceof EqualsTo
				EqualsTo expr =(EqualsTo) select.getWhere();
				if(expr.getLeftExpression() instanceof SubSelect)
				{
					//"Inside Instance of Delete left expression insatnceOF SubSelect
					graph = traverseSelect((SubSelect) expr.getLeftExpression());
					//graph=graph+sel.getSelectBody().
				}
				else if(expr.getRightExpression() instanceof SubSelect)
				{
					//Inside Instance of Delete right expression instanceof Subselect
					graph = traverseSelect((SubSelect) expr.getRightExpression());

				}
				else
				{
					//Inside Instance of Delete ELSE
					Column lft= (Column) expr.getLeftExpression();
					graph+=lft.getTable().getName();
				}
				graph+=" -> "+createTable+" [color=black label=\"RIGHT TABLE CREATED HAVING KEYS IN LEFT\"]";						

			}
			else{
				graph+= createTable+" -> "+createTable+" [color=black label=\"CREATE\"]";
			}
		}

		else{
			graph+= createTable+" -> "+createTable+" [color=black label=\"CREATE\"]";
		}

		return graph;
	}
	 */
	private String sanitizeQuery(String queryStatement) {
		queryStatement=queryStatement.replaceAll("[-]+", "");

		return queryStatement;
	}

	private String parseDeleteStatement(String graph, Statement statement) {

		Delete select = (Delete) statement;

		String delTable=(select.getTable().getName()).toUpperCase();
		if(select.getWhere()!=null)
		{
			if(select.getWhere() instanceof EqualsTo){
				//Inside Instance of Delete: select.getWhere()!=null && (select.getWhere() instanceof EqualsTo
				EqualsTo expr =(EqualsTo) select.getWhere();
				if(expr.getLeftExpression() instanceof SubSelect)
				{
					//"Inside Instance of Delete left expression insatnceOF SubSelect
					graph = traverseSelect((SubSelect) expr.getLeftExpression());
					//graph=graph+sel.getSelectBody().
				}
				else if(expr.getRightExpression() instanceof SubSelect)
				{
					//Inside Instance of Delete right expression instanceof Subselect
					graph = traverseSelect((SubSelect) expr.getRightExpression());

				}
				else
				{
					//Inside Instance of Delete ELSE
					Column lft= (Column) expr.getLeftExpression();
					graph+=lft.getTable().getName();
				}
				graph+=" -> "+delTable+" [color=black label=\"RIGHT TABLE DELETED HAVING KEYS IN LEFT\"]";						

			}
			else{
				graph+= delTable+" -> "+delTable+" [color=black label=\"DELETE\"]";
			}
		}

		else{
			graph+= delTable+" -> "+delTable+" [color=black label=\"DELETE\"]";
		}

		return graph;
	}

	private String parseInsertStatement(String graph, Statement statement) {
		Insert insertStmt = (Insert) statement;
		String tableName = insertStmt.getTable().getFullyQualifiedName();

		if(insertStmt.getSelect()!=null)
		{
			PlainSelect sel=(PlainSelect) insertStmt.getSelect().getSelectBody();

			if((sel.toString().contains("JOIN"))){
				graph =  parseSelect(graph, sel)+((SubSelect) sel.getFromItem()).getAlias().getName()+  "->" +tableName.toUpperCase() +" [color=black label=\"INSERT INTO\"];";
			}
			else{
				graph+=(((Table)sel.getFromItem()).getName()).toUpperCase()+ " -> "+(insertStmt.getTable().getName()).toUpperCase()+" [color=black label=\"INSERT INTO\"]";
			}
		}
		else
		{
			graph+=(insertStmt.getTable().getName()).toUpperCase();
		}
		return graph;
	}

	private String parseUpdateStatement(String graph, Statement statement) {
		Update select = (Update) statement;
		String targetTable=(select.getTables().get(0).getName()).toUpperCase();

		if(select.getSelect()!=null){
			String srcTable=(select.getFromItem().toString()).toUpperCase();
			PlainSelect sel=(PlainSelect) select.getSelect().getSelectBody();
			graph+=(((Table)sel.getFromItem()).getName()).toUpperCase()+ " -> "+targetTable+" [color=black label=\"UPDATED FROM "+srcTable+" table\"]";						
		}
		else{
			graph+= targetTable + " -> " + targetTable + " [color=black label=\"UPDATE"+" \"]";
		}
		return graph;
	}

	private String parseSelectStatement(String graph, Statement statement) {
		Select select = (Select) statement;
		PlainSelect sel = (PlainSelect) select.getSelectBody();
		graph = parseSelect(graph, sel);
		return graph;
	}

	private String parseSelect(String graph, PlainSelect plainSel) {

		PlainSelect plainSelect =  plainSel;
		String subGraph="";

		String fromTable="";
		if(plainSelect.getFromItem() instanceof SubSelect)
		{

			fromTable=traverseSelect((SubSelect) plainSelect.getFromItem());
		}
		else if(plainSelect.getFromItem() instanceof  Table)
		{
			fromTable=((Table)plainSelect.getFromItem()).getName();
		}
		subGraph=fromTable+" -> ";
		if(plainSelect.getJoins()!=null)
		{

			int count =0;
			for (Join joinn : plainSelect.getJoins()) {
				String joinType = (getJoinType(joinn)).toUpperCase();

				if(joinn.getOnExpression()==null)
				{
					if(joinn.getRightItem() instanceof SubSelect)
					{
						subGraph+=((SubSelect)joinn.getRightItem()).getAlias().getName()+ " [color=black label=\""+joinType+"\"]"+traverseSelect((SubSelect)joinn.getRightItem());

					}
					else if(joinn.getRightItem() instanceof Table)
					{
						subGraph+=(((Table)joinn.getRightItem()).getName()).toUpperCase()+" [color=black label=\""+joinType+"\"]";						

					}
				}
				else
				{
					if(joinn.getRightItem() instanceof SubSelect)
					{
						subGraph+=((SubSelect)joinn.getRightItem()).getAlias().getName()+ " [color=black label=\""+joinType+"\"]"+traverseSelect((SubSelect)joinn.getRightItem());

					}
					else if(joinn.getRightItem() instanceof Table)
					{
						subGraph+=(((Table)joinn.getRightItem()).getName()).toUpperCase()+" [color=black label=\""+joinType+"\"]";						

					}


				}




			}
		}
		else
		{
			subGraph =subGraph.substring(0, subGraph.length()-4);

		}
		return subGraph;
	}
	private String parseSelectBkup(String graph, PlainSelect plainSel) {

		PlainSelect plainSelect =  plainSel;
		String subGraph="";

		String fromTable="";
		if(plainSelect.getFromItem() instanceof SubSelect)
		{

			fromTable=traverseSelect((SubSelect) plainSelect.getFromItem());
		}
		else if(plainSelect.getFromItem() instanceof  Table)
		{
			fromTable=((Table)plainSelect.getFromItem()).getName();
		}
		subGraph=fromTable+" -> ";
		if(plainSelect.getJoins()!=null)
		{

			int count =0;
			for (Join joinn : plainSelect.getJoins()) {
				String joinType = (getJoinType(joinn)).toUpperCase();

				if(joinn.getOnExpression()==null)
				{
					if(joinn.getRightItem() instanceof SubSelect)
					{
						subGraph+=((SubSelect)joinn.getRightItem()).getAlias().getName()+ " [color=black label=\""+joinType+"\"]"+traverseSelect((SubSelect)joinn.getRightItem());

					}
					else if(joinn.getRightItem() instanceof Table)
					{
						subGraph+=(((Table)joinn.getRightItem()).getName()).toUpperCase()+" [color=black label=\""+joinType+"\"]";						

					}
				}
				else
				{
					if(joinn.getRightItem() instanceof SubSelect)
					{
						subGraph+=((SubSelect)joinn.getRightItem()).getAlias().getName()+ " [color=black label=\""+joinType+"\"]"+traverseSelect((SubSelect)joinn.getRightItem());

					}
					else if(joinn.getRightItem() instanceof Table)
					{
						subGraph+=(((Table)joinn.getRightItem()).getName()).toUpperCase()+" [color=black label=\""+joinType+"\"]";						

					}


				}




			}
		}
		else
		{
			subGraph =subGraph.substring(0, subGraph.length()-4);

		}
		return subGraph;
	}

	/**
	 * @param graph
	 * @param plainSelect
	 * @param joinn
	 * @param joinType
	 * @return
	 */
	private String equalsJoin(String graph, PlainSelect plainSelect, Join joinn, String joinType) {
		EqualsTo eq=(EqualsTo)joinn.getOnExpression();
		String subgraph="";
		if(eq.getLeftExpression() instanceof SubSelect)
		{
			subgraph += traverseSelect((SubSelect) eq.getLeftExpression());
			/*Column lft= (Column) eq.getRightExpression();
			graph+=(lft.getTable().getName()).toUpperCase()+" -> ";						
			 */
			//graph=graph+sel.getSelectBody().
		}
		else
		{
			subgraph=((Column) eq.getLeftExpression()).getTable().getName();
		}
		String subgraph2="";
		if(eq.getRightExpression() instanceof SubSelect)
		{
			subgraph2 += traverseSelect((SubSelect) eq.getRightExpression());
		}
		else
		{
			subgraph2=((Column) eq.getRightExpression()).getTable().getName();
		}
		String localGraph ="subgraph{"+ subgraph+" -> "+subgraph2+" [color=black label=\""+getJoinType(joinn)+"\"]}";						
		return localGraph;
	}

	private String selectWhere(String graph, PlainSelect plainSelect) {
		EqualsTo eq =(EqualsTo) plainSelect.getWhere();
		String subgraph="";
		if(eq.getLeftExpression() instanceof SubSelect)
		{
			subgraph = traverseSelect((SubSelect) eq.getLeftExpression());
			/*Column lft= (Column) eq.getRightExpression();
			graph+=(lft.getTable().getName()).toUpperCase()+" -> ";						
			 */
			//graph=graph+sel.getSelectBody().
		}
		else
		{
			subgraph+=((Column) eq.getLeftExpression()).getTable().getName();
		}
		String subgraph2="";
		if(eq.getRightExpression() instanceof SubSelect)
		{
			subgraph2 += traverseSelect((SubSelect) eq.getRightExpression());
		}
		else
		{
			subgraph2=((Column) eq.getRightExpression()).getTable().getName();
		}
		String localGraph ="subgraph{"+ subgraph+" -> "+subgraph2+" [color=black label=\"SubSelect\"]}";						

		return localGraph;
	}

	private String traverseSelect(SubSelect sel) {
		PlainSelect pSelect=(PlainSelect)sel.getSelectBody();
		String subGraph = "";
		String fromTable="";
		if(pSelect.getFromItem() instanceof SubSelect)
		{
			fromTable=traverseSelect((SubSelect) pSelect.getFromItem());
		}
		else if(pSelect.getFromItem() instanceof  Table)
		{
			fromTable=((Table)pSelect.getFromItem()).getName();
		}
		subGraph=fromTable+" -> ";

		if(pSelect.getJoins()!=null)
		{
			for (Join joinn : pSelect.getJoins()) {
				String joinType = (getJoinType(joinn)).toUpperCase();
				if(joinn.getOnExpression()==null)
				{
					if(joinn.getRightItem() instanceof SubSelect)
					{
						subGraph+=((SubSelect)joinn.getRightItem()).getAlias().getName()+ " [color=black label=\""+joinType+"\"]"+traverseSelect((SubSelect)joinn.getRightItem());


					}
					else if(joinn.getRightItem() instanceof Table)
					{
						subGraph+=(((Table)joinn.getRightItem()).getName()).toUpperCase()+" [color=black label=\""+joinType+"\"]";						

					}

				}
				else
				{
					if(joinn.getRightItem() instanceof SubSelect)
					{
						subGraph+=((SubSelect)joinn.getRightItem()).getAlias().getName()+ " [color=black label=\""+joinType+"\"]"+traverseSelect((SubSelect)joinn.getRightItem());

					}
					else if(joinn.getRightItem() instanceof Table)
					{
						subGraph+=(((Table)joinn.getRightItem()).getName()).toUpperCase()+" [color=black label=\""+joinType+"\"]";						

					}


				}
			}
		}
		else
		{
			subGraph =subGraph.substring(0, subGraph.length()-4);

		}

		return subGraph;
	}

	private String getJoinType(Join joinn) {
		String joinType="";
		if(joinn.isCross())
		{
			joinType="Cross Join";
		}
		else if(joinn.isFull())
		{
			joinType="Full Join";
		}
		else if(joinn.isInner())
		{
			joinType="Inner Join";

		}
		else if(joinn.isLeft())
		{
			joinType="Left Join";

		}
		else if(joinn.isNatural())
		{
			joinType="Natural Join";

		}
		else if(joinn.isOuter())
		{
			joinType="Outer Join";

		}
		else if(joinn.isRight())
		{
			joinType="Right Join";

		}
		else if(joinn.isSemi())
		{
			joinType="Semi Join";

		}
		else if(joinn.isSimple())
		{
			joinType="Simple Join";

		}
		return joinType.toUpperCase();

	}

	private List<String> getJoinList(String FullQuery){
		List<String> listOfJoins    = new ArrayList<String>();
		String join = null;
		String leftRight = null;
		
		while(FullQuery.contains("join")){
			leftRight = FullQuery.substring((FullQuery.indexOf("join")-12), (FullQuery.indexOf("join")));
 
			if(leftRight.trim().equalsIgnoreCase("LEFT OUTER")){
				join = "LEFT OUTER JOIN";
			}
			else if(leftRight.trim().equalsIgnoreCase("RIGHT OUTER")){
				join = "RIGHT OUTER JOIN";
			} 
			else if(leftRight.trim().equalsIgnoreCase("LEFT INNER")){
				join = "LEFT INNER JOIN";
			} 
			else if(leftRight.trim().equalsIgnoreCase("RIGHT INNER")){
				join = "RIGHT INNER JOIN";
			} 
			else
			{
				leftRight = FullQuery.substring((FullQuery.indexOf("join")-6), (FullQuery.indexOf("join")));
				if(leftRight.trim().equalsIgnoreCase("OUTER")){
					join = "OUTER JOIN";
				}
				else if(leftRight.trim().equalsIgnoreCase("CROSS")){
					join = "CROSS JOIN";
				} 
				else if(leftRight.trim().equalsIgnoreCase("INNER")){
					join = "INNER JOIN";
				} 
				else if(leftRight.trim().equalsIgnoreCase("FULL")){
					join = "FULL JOIN";
				}
				else if(leftRight.trim().equalsIgnoreCase("LEFT")){
					join = "LEFT JOIN";
				}
				else if(leftRight.trim().equalsIgnoreCase("RIGHT")){
					join = "RIGHT JOIN";
				}
				else{
					join = "SIMPLE JOIN";
				}
			}
			listOfJoins.add(join);
			FullQuery = FullQuery.substring((FullQuery.indexOf("join")+1));

		}
		return listOfJoins;
	}

	private String generateGraph(List<String> joinslist, List<String> srcTabList, List<String> tarTableList,List<String> stAloneTabList,String action){
		String graph =" ";
		List<String> srcMapList = new ArrayList<String>();
		
		for(int i=0;i<joinslist.size();i++){
			String joingraph = joinslist.get(i);
			String tempJoin = "[color=black label=\""+joingraph+"\"];";
			joinslist.remove(i);
			joinslist.add(i,tempJoin);
		}
		for(int i=0; i <srcTabList.size()-1 ; i++){
			for(int j=i+1;j<i+2;j++){
				if(srcTabList.get(i).contains("$")){
					String tmpSrc = srcTabList.get(i);
					srcTabList.remove(i);
					srcTabList.add(i, tmpSrc.substring(tmpSrc.indexOf(".")+1));
				}
				if(srcTabList.get(j).contains("$")){
					String tmpSrc = srcTabList.get(j);
					srcTabList.remove(j);
					srcTabList.add(j, tmpSrc.substring(tmpSrc.indexOf(".")+1));
				}
				srcMapList.add(srcTabList.get(i) + "->" + srcTabList.get(j));
			}
		}

		for(int i=0;i<srcMapList.size();i++){
			if(i<joinslist.size())
				graph += srcMapList.get(i) + joinslist.get(i);
			else
				break;
		}
		System.out.println("graph " + graph);

		if(srcTabList.isEmpty() && tarTableList.isEmpty() && stAloneTabList.isEmpty()){
			graph = "";
		}
		else if(srcTabList.isEmpty() && !(tarTableList.isEmpty())){
			if(tarTableList.get(0).contains("$")){
				String tmpSrc = tarTableList.get(0);
				tarTableList.remove(0);
				tarTableList.add(0, tmpSrc.substring(tmpSrc.indexOf(".")+1));
			}

			if(action.equalsIgnoreCase("I")){
				graph += tarTableList.get(0) + "-> " +tarTableList.get(0) + " [color=black label=\"INSERT INTO\"];";
			}
			else if(action.equalsIgnoreCase("U")){
				graph += tarTableList.get(0) + "-> " +tarTableList.get(0) + " [color=black label=\"UPDATE\"];";
			}
		}
		else if(srcTabList.isEmpty() && !(stAloneTabList.isEmpty())){
			if(stAloneTabList.get(0).contains("$")){
				String tmpSrc = stAloneTabList.get(0);
				stAloneTabList.remove(0);
				stAloneTabList.add(0, tmpSrc.substring(tmpSrc.indexOf(".")+1));
			}
			if(action.equalsIgnoreCase("C")){
				graph += stAloneTabList.get(0) + "-> " +stAloneTabList.get(0) + " [color=black label=\"CREATE\"];";
			}
			else if(action.equalsIgnoreCase("CV")){
				graph += stAloneTabList.get(0) + "-> " +stAloneTabList.get(0) + " [color=black label=\"CREATE-VOLATILE\"];";
			}
			else if(action.equalsIgnoreCase("D")){
				graph += stAloneTabList.get(0) + "-> " +stAloneTabList.get(0) + " [color=black label=\"DELETE\"];";
			}
		}
		else if(!(srcTabList.isEmpty()))

		{
			if(srcTabList.get(srcTabList.size()-1).contains("$")){
				String tmpSrc = srcTabList.get(srcTabList.size()-1);
				srcTabList.remove(srcTabList.size()-1);
				if(srcTabList.size() == 0)
					srcTabList.add(0, tmpSrc.substring(tmpSrc.indexOf(".")+1));
				else
					srcTabList.add((srcTabList.size()-1), tmpSrc.substring(tmpSrc.indexOf(".")+1));
			}

			if(!(tarTableList.isEmpty()))	{
				if(tarTableList.get(0).contains("$")){
					String tmpSrc = tarTableList.get(0);
					tarTableList.remove(0);
					tarTableList.add(0, tmpSrc.substring(tmpSrc.indexOf(".")+1));
				}
				if(action.equalsIgnoreCase("I")){
					graph += srcTabList.get(srcTabList.size()-1) + "-> " +tarTableList.get(0) + " [color=black label=\"INSERT INTO\"];";
				}
				else if(action.equalsIgnoreCase("U")){
					graph += srcTabList.get(srcTabList.size()-1) + "-> " +tarTableList.get(0) + " [color=black label=\"UPDATE\"];";
				}
				else if(action.equalsIgnoreCase("MU")){
					graph += srcTabList.get(srcTabList.size()-1) + "-> " +tarTableList.get(0) + " [color=black label=\"MERGE-UPDATE\"];";
				}
				else if(action.equalsIgnoreCase("MI")){
					graph += srcTabList.get(srcTabList.size()-1) + "-> " +tarTableList.get(0) + " [color=black label=\"MERGE-INSERT\"];";
				}
			}
		
		else if(!(stAloneTabList.isEmpty())){
			if(stAloneTabList.get(0).contains("$")){
				String tmpSrc = stAloneTabList.get(0);
				stAloneTabList.remove(0);
				stAloneTabList.add(0, tmpSrc.substring(tmpSrc.indexOf(".")+1));
			}

			if(action.equalsIgnoreCase("C")){
				graph += srcTabList.get(srcTabList.size()-1) + "-> " +stAloneTabList.get(0) + " [color=black label=\"CREATE\"];";
			}
			else if(action.equalsIgnoreCase("CV")){
				graph += srcTabList.get(srcTabList.size()-1) + "-> " +stAloneTabList.get(0) + " [color=black label=\"CREATE-VOLATILE\"];";
			}
			else if(action.equalsIgnoreCase("D")){
				graph += srcTabList.get(srcTabList.size()-1) + "-> " +stAloneTabList.get(0) + " [color=black label=\"DELETE\"];";
			}
		}
	}
	
		return graph;
	}
	
}
