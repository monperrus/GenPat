/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.stmt;

import mfix.common.util.LevelLogger;
import mfix.core.node.NodeUtils;
import mfix.core.node.match.MatchLevel;
import mfix.core.node.ast.Node;
import mfix.core.node.ast.VarScope;
import mfix.core.node.ast.expr.SName;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Adaptee;
import mfix.core.node.modify.Modification;
import mfix.core.node.modify.Update;
import mfix.core.pattern.cluster.NameMapping;
import mfix.core.pattern.cluster.VIndex;
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
public class BreakStmt extends Stmt {

	private static final long serialVersionUID = 228415180803512647L;
	private SName _identifier = null;
	
	/**
	 * BreakStatement:
     *	break [ Identifier ] ;
	 */
	public BreakStmt(String fileName, int startLine, int endLine, ASTNode node) {
		this(fileName, startLine, endLine, node, null);
	}
	
	public BreakStmt(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
		super(fileName, startLine, endLine, node, parent);
		_nodeType = TYPE.BREACK;
		_fIndex = VIndex.STMT_BREAK;
	}
	
	public void setIdentifier(SName identifier){
		_identifier = identifier;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("break");
		if(_identifier != null){
			stringBuffer.append(" ");
			stringBuffer.append(_identifier.toSrcString());
		}
		stringBuffer.append(";");
		return stringBuffer;
	}

	@Override
	protected StringBuffer toFormalForm0(NameMapping nameMapping, boolean parentConsidered, Set<String> keywords) {
		if (isAbstract() && !isConsidered()) return null;
		StringBuffer identifier = _identifier == null ? null : _identifier.formalForm(nameMapping, isConsidered(), keywords);
		if (identifier == null) {
			if (isConsidered()) {
				return new StringBuffer("break ")
						.append(_identifier == null ? "" : nameMapping.getExprID(_identifier)).append(';');
			}
			return null;
		}
		return new StringBuffer("break ").append(identifier).append(';');
	}

	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
		if(_identifier != null) {
			_tokens.addAll(_identifier.tokens());
		}
		_tokens.add(";");
	}
	
	@Override
	public List<Stmt> getChildren() {
		return new ArrayList<>(0);
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
	public boolean compare(Node other) {
		boolean match = false;
		if (other != null && other instanceof BreakStmt) {
			BreakStmt breakStmt = (BreakStmt) other;
			match = _identifier == null ? (breakStmt._identifier == null) : _identifier.compare(breakStmt._identifier);
		}
		return match;
	}

	@Override
	public void computeFeatureVector() {
		_selfFVector = new FVector();
		_selfFVector.inc(FVector.KEY_BREAK);

		_completeFVector = new FVector();
		_completeFVector.inc(FVector.KEY_BREAK);
		if(_identifier != null) {
            _completeFVector.combineFeature(_identifier.getFeatureVector());
        }
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		boolean match = false;
		BreakStmt breakStmt = null;
		if (getBindingNode() != null && (getBindingNode() == node || !compare(node))) {
			breakStmt = (BreakStmt) getBindingNode();
			match = (breakStmt == node);
		} else if (canBinding(node)) {
			breakStmt = (BreakStmt) node;
			setBindingNode(node);
			match = true;
		}
		if(breakStmt != null && _identifier != null) {
			_identifier.postAccurateMatch(breakStmt._identifier);
		}
		return match;
	}

	@Override
	public boolean genModifications() {
		if (super.genModifications()) {
			BreakStmt breakStmt = (BreakStmt) getBindingNode();
			if (_identifier == null) {
				if (breakStmt._identifier != null) {
					Update update = new Update(this, _identifier, breakStmt._identifier);
					_modifications.add(update);
				}
			} else if (_identifier.getBindingNode() != breakStmt._identifier){
				Update update = new Update(this, _identifier, breakStmt._identifier);
				_modifications.add(update);
			} else {
				_identifier.genModifications();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings, MatchLevel level) {
		if(node instanceof BreakStmt) {
			BreakStmt breakStmt = (BreakStmt) node;
			if(super.ifMatch(node, matchedNode, matchedStrings, level)) {
				if (_identifier != null && breakStmt._identifier != null) {
					matchedNode.put(_identifier, breakStmt._identifier);
					matchedStrings.put(_identifier.toString(), breakStmt._identifier.toString());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public StringBuffer transfer(VarScope vars, Map<String, String> exprMap, String retType, Set<String> exceptions,
                                 Adaptee metric) {
		StringBuffer stringBuffer = super.transfer(vars, exprMap, retType, exceptions, metric);
		if (stringBuffer == null) {
			metric.inc();
			stringBuffer = new StringBuffer("break");
			if(_identifier != null){
				stringBuffer.append(" ");
				StringBuffer tmp = _identifier.adaptModifications(vars, exprMap, retType, exceptions, metric);
				if(tmp == null) return null;
				stringBuffer.append(tmp);
			}
			stringBuffer.append(";");
		}
		return stringBuffer;
	}

	@Override
	public StringBuffer adaptModifications(VarScope vars, Map<String, String> exprMap, String retType,
                                           Set<String> exceptions, Adaptee metric) {
		StringBuffer identifier = null;
		Node pnode = NodeUtils.checkModification(this);
		if (pnode != null) {
			BreakStmt breakStmt = (BreakStmt) pnode;
			for (Modification modification : breakStmt.getModifications()) {
				if (modification instanceof Update) {
					Update update = (Update) modification;
					if (update.getSrcNode() == breakStmt._identifier) {
						identifier = update.apply(vars, exprMap, retType, exceptions, metric);
						if (identifier == null) return null;
					} else {
						LevelLogger.error("@BreakStmt ERROR");
					}
				} else {
					LevelLogger.error("@BreakStmt Should not be this kind of modification : " + modification);
				}
			}
		}
		StringBuffer stringBuffer = new StringBuffer("break");
		if (identifier == null) {
			if (_identifier != null) {
				stringBuffer.append(" ");
				StringBuffer tmp = _identifier.adaptModifications(vars, exprMap, retType, exceptions, metric);
				if(tmp == null) return null;
				stringBuffer.append(tmp);
			}
		} else {
			stringBuffer.append(" " + identifier);
		}
		stringBuffer.append(";");
		return stringBuffer;
	}
}
