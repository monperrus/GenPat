/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.stmt;

import mfix.core.node.ast.Node;
import mfix.core.node.ast.expr.SName;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Update;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class ContinueStmt extends Stmt {

	private static final long serialVersionUID = -4634975771051671527L;
	private SName _identifier = null;
	
	/**
	 * ContinueStatement:
     *	continue [ Identifier ] ;
	 */
	public ContinueStmt(String fileName, int startLine, int endLine, ASTNode node) {
		this(fileName, startLine, endLine, node, null);
	}

	public ContinueStmt(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
		super(fileName, startLine, endLine, node, parent);
		_nodeType = TYPE.CONTINUE;
	}
	
	public void setIdentifier(SName identifier){
		_identifier = identifier;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("continue");
		if(_identifier != null){
			stringBuffer.append(" ");
			stringBuffer.append(_identifier.toSrcString());
		}
		stringBuffer.append(";");
		return stringBuffer;
	}
	
	protected void tokenize() {
		_tokens = new LinkedList<>();
		_tokens.add("continue");
		if(_identifier != null) {
			_tokens.addAll(_identifier.tokens());
		}
		_tokens.add(";");
	}
	
	@Override
	public List<Node> getAllChildren() {
		List<Node> children = new ArrayList<>(1);
		if(_identifier != null) {
			children.add(_identifier);
		}
		return children;
	}
	
	@Override
	public List<Stmt> getChildren() {
		return new ArrayList<>(0);
	}
	
	@Override
	public boolean compare(Node other) {
		boolean match = false;
		if(other instanceof ContinueStmt) {
			ContinueStmt continueStmt = (ContinueStmt) other;
			match = (_identifier == null) ? (continueStmt._identifier == null)
					: _identifier.compare(continueStmt._identifier);
		}
		return match;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new FVector();
		_fVector.inc(FVector.KEY_CONTINUE);
		if(_identifier != null) {
			_fVector.combineFeature(_identifier.getFeatureVector());
		}
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		boolean match = false;
		ContinueStmt continueStmt = null;
		if(getBindingNode() != null) {
			continueStmt = (ContinueStmt) getBindingNode();
			match = (continueStmt == node);
		} else if(canBinding(node)) {
			continueStmt = (ContinueStmt) node;
			setBindingNode(node);
			match = true;
		}
		if(continueStmt != null && _identifier != null) {
			_identifier.postAccurateMatch(continueStmt._identifier);
		}
		return match;
	}

	@Override
	public boolean genModidications() {
		if(super.genModidications()) {
			ContinueStmt continueStmt = (ContinueStmt) getBindingNode();
			if(_identifier == null) {
				if(continueStmt._identifier != null) {
					Update update = new Update(this, _identifier, continueStmt._identifier);
					_modifications.add(update);
				}
			} else if (_identifier.getBindingNode() != continueStmt._identifier){
				Update update = new Update(this, _identifier, continueStmt._identifier);
				_modifications.add(update);
			} else {
				_identifier.genModidications();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings) {
		if(node instanceof ContinueStmt) {
			ContinueStmt continueStmt = (ContinueStmt) node;
			if(super.ifMatch(node, matchedNode, matchedStrings)) {
				if (_identifier != null && continueStmt._identifier != null) {
					matchedNode.put(_identifier, continueStmt._identifier);
					matchedStrings.put(_identifier.toString(), continueStmt._identifier.toString());
				}
				return true;
			}
		}
		return false;
	}
}