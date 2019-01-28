/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.stmt;

import mfix.core.node.ast.Node;
import mfix.core.node.match.metric.FVector;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class AssertStmt extends Stmt {

	private static final long serialVersionUID = 8494694375316529776L;
	private String _codeStr;
	// TODO: assert statement should be added
	/**
	 * AssertStatement:
     *	assert Expression [ : Expression ] ;
	 */
	public AssertStmt(String fileName, int startLine, int endLine, ASTNode node) {
		this(fileName, startLine, endLine, node, null);
	}

	public AssertStmt(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
		super(fileName, startLine, endLine, node, parent);
		_nodeType = TYPE.ASSERT;
		_codeStr = node.toString();
	}

	@Override
	public StringBuffer toSrcString() {
		return new StringBuffer(_codeStr);
	}

	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
		_tokens.add(_codeStr);
	}

	@Override
	public List<Stmt> getChildren() {
		return new ArrayList<>(0);
	}

	@Override
	public boolean compare(Node other) {
		if (other instanceof AssertStmt) {
			return _codeStr.equals(((AssertStmt) other)._codeStr);
		}
		return false;
	}

	@Override
	public List<Node> getAllChildren() {
		return new ArrayList<>(0);
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new FVector();
		_fVector.inc(FVector.KEY_ASSERT);
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		if (getBindingNode() == node) return true;
		if (getBindingNode() == null && canBinding(node)) {
			setBindingNode(node);
			return true;
		}
		return false;
	}

	@Override
	public boolean genModidications() {
		return true;
	}

	@Override
	public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings) {
		if(node instanceof AssertStmt) {
			return super.ifMatch(node, matchedNode, matchedStrings);
		}
		return false;
	}

	@Override
	public StringBuffer transfer(Set<String> vars) {
		StringBuffer stringBuffer = super.transfer(vars);
		if (stringBuffer == null) {
			stringBuffer = toSrcString();
		}
		return stringBuffer;
	}

	@Override
	public StringBuffer adaptModifications(Set<String> vars) {
		return toSrcString();
	}
}