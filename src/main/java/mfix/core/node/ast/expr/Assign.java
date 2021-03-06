/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.expr;

import mfix.common.util.LevelLogger;
import mfix.core.node.NodeUtils;
import mfix.core.node.match.MatchLevel;
import mfix.core.node.ast.Node;
import mfix.core.node.ast.VarScope;
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
public class Assign extends Expr {

    private static final long serialVersionUID = 508933142391046341L;
    private Expr _lhs = null;
    private AssignOperator _operator = null;
    private Expr _rhs = null;

    /**
     * Assignment:
     *      Expression AssignmentOperator Expression
     */
    public Assign(String fileName, int startLine, int endLine, ASTNode node) {
        super(fileName, startLine, endLine, node);
        _nodeType = TYPE.ASSIGN;
        _fIndex = VIndex.EXP_ASSIGN;
    }

    public void setLeftHandSide(Expr lhs) {
        _lhs = lhs;
    }

    public void setOperator(AssignOperator operator) {
        _operator = operator;
    }

    public void setRightHandSide(Expr rhs) {
        _rhs = rhs;
    }

    public AssignOperator getOperator() {
        return _operator;
    }

    public Expr getLhs() {
        return _lhs;
    }

    public Expr getRhs() {
        return _rhs;
    }

    @Override
    public List<Node> getAllChildren() {
        List<Node> children = new ArrayList<>(3);
        children.add(_lhs);
        children.add(_operator);
        children.add(_rhs);
        return children;
    }

    @Override
    public StringBuffer toSrcString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(_lhs.toSrcString());
        stringBuffer.append(_operator.toSrcString());
        stringBuffer.append(_rhs.toSrcString());
        return stringBuffer;
    }

    @Override
    protected StringBuffer toFormalForm0(NameMapping nameMapping, boolean parentConsidered, Set<String> keywords) {
//        boolean consider = isConsidered() || parentConsidered;
        boolean consider = isConsidered();
        StringBuffer lhs = _lhs.formalForm(nameMapping, consider, keywords);
        StringBuffer op = _operator.formalForm(nameMapping, consider, keywords);
        StringBuffer rhs = _rhs.formalForm(nameMapping, consider, keywords);
        if (op == null && lhs == null && rhs == null) {
            return super.toFormalForm0(nameMapping, parentConsidered, keywords);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(lhs == null ? nameMapping.getExprID(_lhs) : lhs)
                .append(op == null ? _operator.getOperatorStr() : op)
                .append(rhs == null ? nameMapping.getExprID(_rhs) : rhs);
        return buffer;
    }

    @Override
    protected void tokenize() {
        _tokens = new LinkedList<>();
        _tokens.addAll(_lhs.tokens());
        _tokens.addAll(_operator.tokens());
        _tokens.addAll(_rhs.tokens());
    }

    @Override
    public boolean compare(Node other) {
        boolean match = false;
        if (other != null && other instanceof Assign) {
            Assign assign = (Assign) other;
            match = _operator.compare(assign._operator) && _lhs.compare(assign._lhs) && _rhs.compare(assign._rhs);
        }
        return match;
    }

    @Override
    public void computeFeatureVector() {
        _selfFVector = new FVector();
        _selfFVector.inc(FVector.E_ASSIGN);

        _completeFVector = new FVector();
        _completeFVector.combineFeature(_selfFVector);
        _completeFVector.combineFeature(_lhs.getFeatureVector());
        _completeFVector.combineFeature(_rhs.getFeatureVector());
    }

    @Override
    public boolean patternMatch(Node node, Map<Node, Node> matchedNode) {
        if (node != null && node.getNodeType() == getNodeType()) {
            return super.patternMatch(node, matchedNode);
        }
        return false;
    }

    @Override
    public boolean postAccurateMatch(Node node) {
        boolean match = false;
        Assign assign = null;
        if (compare(node)) {
            assign = (Assign) node;
            setBindingNode(node);
            match = true;
        } else if (getBindingNode() != null) {
            assign = (Assign) getBindingNode();
            match = (assign == node);
        } else if (canBinding(node)) {
            assign = (Assign) node;
            setBindingNode(node);
            match = true;
        }

        if (assign == null) {
            continueTopDownMatchNull();
        } else {
            _lhs.postAccurateMatch(assign.getLhs());
            _operator.postAccurateMatch(assign.getOperator());
            _rhs.postAccurateMatch(assign.getRhs());
        }

        return match;
    }

    @Override
    public boolean genModifications() {
        if (super.genModifications()) {
            Assign assign = (Assign) getBindingNode();
            if (_lhs.getBindingNode() != assign.getLhs()) {
                Update update = new Update(this, _lhs, assign.getLhs());
                _modifications.add(update);
            } else {
                _lhs.genModifications();
            }
            if (!_operator.compare(assign.getOperator())) {
                Update update = new Update(this, _operator, assign.getOperator());
                _modifications.add(update);
            }
            if (_rhs.getBindingNode() != assign.getRhs()) {
                Update update = new Update(this, _rhs, assign.getRhs());
                _modifications.add(update);
            } else {
                _rhs.genModifications();
            }
        }
        return true;
    }

    @Override
    public void greedyMatchBinding(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings) {
        if (node instanceof Assign) {
            Assign assign = (Assign) node;
            if (NodeUtils.matchSameNodeType(_lhs, assign.getLhs(), matchedNode, matchedStrings)
                    && NodeUtils.matchSameNodeType(_rhs, assign.getRhs(), matchedNode, matchedStrings)) {
                matchedNode.put(_operator, assign.getOperator());
                getLhs().greedyMatchBinding(assign.getLhs(), matchedNode, matchedStrings);
                getRhs().greedyMatchBinding(assign.getRhs(), matchedNode, matchedStrings);
            }
        }
    }

    @Override
    public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings, MatchLevel level) {
        if (super.ifMatch(node, matchedNode, matchedStrings, level)) {
            if (node instanceof Assign) {
                Assign assign = (Assign) node;
                if (NodeUtils.matchSameNodeType(_lhs, assign.getLhs(), matchedNode, matchedStrings)
                        && NodeUtils.checkDependency(_rhs, assign.getRhs(), matchedNode, matchedStrings, level)
                        && NodeUtils.matchSameNodeType(_rhs, assign.getRhs(), matchedNode, matchedStrings)) {
                    matchedNode.put(_operator, assign.getOperator());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public StringBuffer transfer(VarScope vars, Map<String, String> exprMap, String retType, Set<String> exceptions,
                                 Adaptee metric) {
        StringBuffer stringBuffer = super.transfer(vars, exprMap, retType, exceptions, metric);
        if (stringBuffer == null) {
            stringBuffer = new StringBuffer();
            StringBuffer tmp;
            tmp = _lhs.transfer(vars, exprMap, retType, exceptions, metric);
            if (tmp == null || !NodeUtils.isLegalVar(tmp.toString())) return null;
            stringBuffer.append(tmp);
            tmp = _operator.transfer(vars, exprMap, retType, exceptions, metric);
            if (tmp == null) return null;
            stringBuffer.append(tmp);
            tmp = _rhs.transfer(vars, exprMap, retType, exceptions, metric);
            if (tmp == null) return null;
            stringBuffer.append(tmp);
        }
        return stringBuffer;
    }

    @Override
    public StringBuffer adaptModifications(VarScope vars, Map<String, String> exprMap, String retType,
                                           Set<String> exceptions, Adaptee metric) {
        StringBuffer operator = null;
        StringBuffer lhs = null;
        StringBuffer rhs = null;
        Node node = NodeUtils.checkModification(this);
        if (node != null) {
            Assign assign = (Assign) node;
            for (Modification modification : assign.getModifications()) {
                if (modification instanceof Update) {
                    Update update = (Update) modification;
                    if (update.getSrcNode() == assign._operator) {
                        operator = update.apply(vars, exprMap, retType, exceptions, metric);
                        if (operator == null) return null;
                    } else if (update.getSrcNode() == assign._lhs) {
                        lhs = update.apply(vars, exprMap, retType, exceptions, metric);
                        if (lhs == null) return null;
                    } else {
                        rhs = update.apply(vars, exprMap, retType, exceptions, metric);
                        if (rhs == null) return null;
                    }
                } else {
                    LevelLogger.error("@Assign Should not be this kind of modification : " + modification.toString());
                }

            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer tmp;
        if(lhs == null) {
            tmp = _lhs.adaptModifications(vars, exprMap, retType, exceptions, metric);
            if(tmp == null || !NodeUtils.isLegalVar(tmp.toString())) return null;
            stringBuffer.append(tmp);
        } else {
            stringBuffer.append(lhs);
        }
        if(operator == null) {
            tmp = _operator.adaptModifications(vars, exprMap, retType, exceptions, metric);
            if(tmp == null) return null;
            stringBuffer.append(tmp);
        } else {
            stringBuffer.append(operator);
        }
        if(rhs == null) {
            tmp = _rhs.adaptModifications(vars, exprMap, retType, exceptions, metric);
            if(tmp == null) return null;
            stringBuffer.append(tmp);
        } else {
            stringBuffer.append(rhs);
        }
        return stringBuffer;
    }
}
