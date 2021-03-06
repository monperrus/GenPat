/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.stmt;

import mfix.common.conf.Constant;
import mfix.core.node.NodeUtils;
import mfix.core.node.match.MatchLevel;
import mfix.core.node.ast.Node;
import mfix.core.node.ast.VarScope;
import mfix.core.node.ast.expr.VarDeclarationExpr;
import mfix.core.node.match.Matcher;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Adaptee;
import mfix.core.node.modify.Modification;
import mfix.core.node.modify.Update;
import mfix.core.pattern.cluster.NameMapping;
import mfix.core.pattern.cluster.VIndex;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class TryStmt extends Stmt {

	private static final long serialVersionUID = -4283593302346047974L;
	private List<VarDeclarationExpr> _resource = null;
	private Blk _blk = null;
	private List<CatClause> _catches = null;
	private Blk _finallyBlk = null;
	
	/**
	 * TryStatement:
     *	try [ ( Resources ) ]
     *	    Block
     *	    [ { CatchClause } ]
     *	    [ finally Block ]
	 */
	public TryStmt(String fileName, int startLine, int endLine, ASTNode node) {
		this(fileName, startLine, endLine, node, null);
	}

	public TryStmt(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
		super(fileName, startLine, endLine, node, parent);
		_nodeType = TYPE.TRY;
		_fIndex = VIndex.STMT_TRY;
	}
	
	public void setResource(List<VarDeclarationExpr> resource) {
		_resource = resource;
	}
	
	public void setBody(Blk blk){
		_blk = blk;
	}
	
	public void setCatchClause(List<CatClause> catches) {
		_catches = catches;
	}
	
	public void setFinallyBlock(Blk finallyBlk) {
		_finallyBlk = finallyBlk;
	}

	public List<VarDeclarationExpr> getResource() {
		return _resource;
	}

	public Blk getBody() {
		return _blk;
	}

	public List<CatClause> getCatches() {
		return _catches;
	}

	public Blk getFinally() {
		return _finallyBlk;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer("try");
		if(_resource != null && _resource.size() > 0) {
			stringBuffer.append("(");
			stringBuffer.append(_resource.get(0).toSrcString());
			for(int i = 1; i < _resource.size(); i++) {
				stringBuffer.append(";");
				stringBuffer.append(_resource.get(i).toSrcString());
			}
			stringBuffer.append(")");
		}
		stringBuffer.append(_blk.toSrcString());
		if(_catches != null) {
			for(CatClause catClause : _catches) {
				stringBuffer.append(catClause.toSrcString());
			}
		}
		if(_finallyBlk != null) {
			stringBuffer.append("finally");
			stringBuffer.append(_finallyBlk.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	protected StringBuffer toFormalForm0(NameMapping nameMapping, boolean parentConsidered, Set<String> keywords) {
		if (isAbstract() && !isConsidered()) return null;
		StringBuffer res = null;
		boolean hasRes = false;
		if (_resource != null && _resource.size() > 0) {
			res = new StringBuffer("(");
			StringBuffer b = _resource.get(0).formalForm(nameMapping, isConsidered(), keywords);
			if (b == null) {
				res.append(nameMapping.getExprID(_resource.get(0)));
			} else {
				hasRes = true;
				res.append(b);
			}
			for (int i = 1; i < _resource.size(); i++) {
				res.append(';');
				b = _resource.get(i).formalForm(nameMapping, isConsidered(), keywords);
				if (b != null) {
					hasRes = true;
					res.append(b);
				} else {
					res.append(nameMapping.getExprID(_resource.get(0)));
				}
			}
			res.append(')');
		}
		StringBuffer blk = _blk.formalForm(nameMapping, false, keywords);
		StringBuffer catches = null;
		boolean hasCatch = false;
		if (_catches != null && _catches.size() > 0) {
			catches = new StringBuffer();
			CatClause cc;
			StringBuffer b;
			for (int i = 0; i < _catches.size(); i++) {
				b = _catches.get(i).formalForm(nameMapping, false, keywords);
				if (b != null) {
					hasCatch = true;
					catches.append('\n').append(b);
				} else {
					cc = _catches.get(i);
					catches.append("\ncatch(")
							.append(nameMapping.getTypeID(cc.getException().getDeclType()))
							.append(' ')
							.append(nameMapping.getExprID(cc.getException().getName()))
							.append("){}");
				}
			}
		}
		StringBuffer finalblk = _finallyBlk == null ? null : _finallyBlk.formalForm(nameMapping, false, keywords);
		if (isConsidered() || hasRes || blk != null || hasCatch || finalblk != null) {
			StringBuffer buffer = new StringBuffer("try");
			buffer.append(res == null ? "" : res)
					.append(blk == null ? "{}" : blk)
					.append(catches == null ? "" : catches);
			if (finalblk != null) {
				buffer.append('\n').append("finally").append(finalblk);
			}
			return buffer;
		}
		return null;
	}

	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
		_tokens.add("try");
		if(_resource != null && _resource.size() > 0) {
			_tokens.add("(");
			_tokens.addAll(_resource.get(0).tokens());
			for(int i = 1; i < _resource.size(); i++) {
				_tokens.add(";");
				_tokens.addAll(_resource.get(i).tokens());
			}
			_tokens.add(")");
		}
		_tokens.addAll(_blk.tokens());
		if(_catches != null) {
			for(CatClause catClause : _catches) {
				_tokens.addAll(catClause.tokens());
			}
		}
		if(_finallyBlk != null) {
			_tokens.add("finally");
			_tokens.addAll(_finallyBlk.tokens());
		}
	}
	
	@Override
	public List<Node> getAllChildren() {
		List<Node> children = new ArrayList<>();
		if(_resource != null) {
			children.addAll(_resource);
		}
		children.add(_blk);
		if(_catches != null) {
			children.addAll(_catches);
		}
		if(_finallyBlk != null) {
			children.add(_finallyBlk);
		}
		return children;
	}
	
	@Override
	public List<Stmt> getChildren() {
		List<Stmt> children = new ArrayList<>(1);
		children.add(_blk);
		return children;
	}

	@Override
	public List<Node> wrappedNodes() {
		List<Node> result = new LinkedList<>();
		for (Stmt stmt : _blk.getStatement()) {
			if (stmt.getBindingNode() == null) {
				return null;
			}
			result.add(stmt.getBindingNode());
		}
		return result;
	}

	@Override
	public boolean compare(Node other) {
		boolean match = false;
		if(other != null && other instanceof TryStmt) {
			TryStmt tryStmt = (TryStmt) other;
			if(_resource == null) {
				match = (tryStmt._resource == null);
			} else {
				if(tryStmt._resource == null) {
					match = false;
				} else {
					match = (_resource.size() == tryStmt._resource.size());
					for(int i = 0; match && i < _resource.size(); i++) {
						match = match && _resource.get(i).compare(tryStmt._resource.get(i));
					}
				}
			}
			// body
			match = match && _blk.compare(tryStmt._blk);
			// catch clause
			if(_catches != null) {
				if(tryStmt._catches != null) {
					match = match && (_catches.size() == tryStmt._catches.size());
					for(int i = 0; match && i < _catches.size(); i ++) {
						match = match && _catches.get(i).compare(tryStmt._catches.get(i));
					}
				} else {
					match = false;
				}
			} else {
				match = match && (tryStmt._catches == null);
			}
			// finally block
			if(_finallyBlk == null) {
				match = match && (tryStmt._finallyBlk == null);
			} else {
				if(tryStmt._finallyBlk == null) {
					match = false;
				} else {
					match = match && _finallyBlk.compare(tryStmt._finallyBlk);
				}
			}
		}
		return match;
	}
	
	@Override
	public void computeFeatureVector() {
		_selfFVector = new FVector();
		_selfFVector.inc(FVector.KEY_TRY);

		_completeFVector = new FVector();
		_completeFVector.inc(FVector.KEY_TRY);
		if(_resource != null) {
			for(VarDeclarationExpr vdExpr : _resource) {
				_completeFVector.combineFeature(vdExpr.getFeatureVector());
			}
		}
		_completeFVector.combineFeature(_blk.getFeatureVector());
		if(_catches != null) {
			for(CatClause catClause : _catches) {
				_completeFVector.combineFeature(catClause.getFeatureVector());
			}
		}
		if(_finallyBlk != null) {
			_completeFVector.combineFeature(_finallyBlk.getFeatureVector());
		}
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		boolean match = false;
		TryStmt tryStmt = null;
		if (getBindingNode() != null && (getBindingNode() == node || !compare(node))) {
			tryStmt = (TryStmt) getBindingNode();
			match = (tryStmt == node);
		} else if(canBinding(node)) {
			tryStmt = (TryStmt) node;
			setBindingNode(tryStmt);
			match = true;
		}
		if(tryStmt == null) {
			continueTopDownMatchNull();
		} else {
			if(_resource != null && tryStmt.getResource() != null) {
				NodeUtils.greedyMatchListNode(_resource, tryStmt.getResource());
			}
			_blk.postAccurateMatch(tryStmt.getBody());
			if(_catches != null && tryStmt.getCatches() != null) {
				NodeUtils.greedyMatchListNode(_catches, tryStmt.getCatches());
            }
			if(_finallyBlk != null) {
			    _finallyBlk.postAccurateMatch(tryStmt.getFinally());
            }
		}

		return match;
	}

	@Override
	public boolean genModifications() {
		if (super.genModifications()) {
            TryStmt tryStmt = (TryStmt) getBindingNode();
            _blk.genModifications();
            _modifications.addAll(NodeUtils.genModificationList(this, _catches, tryStmt.getCatches()));
            if (_finallyBlk == null) {
                if (tryStmt.getFinally() != null) {
                    Update update = new Update(this, _finallyBlk, tryStmt.getFinally());
                    _modifications.add(update);
                }
            } else if (_finallyBlk.getBindingNode() != tryStmt.getFinally()) {
                Update update = new Update(this, _finallyBlk, tryStmt.getFinally());
                _modifications.add(update);
            } else {
                _finallyBlk.genModifications();
            }
            return true;
        }
		return false;
	}

	@Override
	public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings, MatchLevel level) {
		if (node instanceof TryStmt) {
			TryStmt tryStmt = (TryStmt) node;
			boolean match = _blk.ifMatch(tryStmt.getBody(), matchedNode, matchedStrings, level);
			if(_finallyBlk != null && tryStmt.getFinally() != null) {
				match = match && _finallyBlk.ifMatch(tryStmt.getFinally(), matchedNode, matchedStrings, level);
			}
			return match && super.ifMatch(node, matchedNode, matchedStrings, level);
		}
		return false;
	}

	@Override
	public StringBuffer transfer(VarScope vars, Map<String, String> exprMap, String retType, Set<String> exceptions,
                                 List<Node> nodes, Adaptee metric) {
		StringBuffer stringBuffer = super.transfer(vars, exprMap, retType, exceptions, metric);
		if (stringBuffer == null) {
			stringBuffer = new StringBuffer("try");
			StringBuffer tmp;
			if (_resource != null && _resource.size() > 0) {
				stringBuffer.append("(");
				tmp = _resource.get(0).transfer(vars, exprMap, retType, exceptions, metric);
				if (tmp == null) return null;
				stringBuffer.append(tmp);
				for (int i = 1; i < _resource.size(); i++) {
					stringBuffer.append(";");
					tmp = _resource.get(i).transfer(vars, exprMap, retType, exceptions, metric);
					if (tmp == null) return null;
					stringBuffer.append(tmp);
				}
				stringBuffer.append(")");
			}
			if (nodes == null) {
				tmp = _blk.transfer(vars, exprMap, retType, exceptions, metric);
				if (tmp == null) return null;
				stringBuffer.append(tmp);
			} else {
				stringBuffer.append("{").append(Constant.NEW_LINE);
				for (Node node : nodes) {
					stringBuffer.append(node.toSrcString().toString()).append(Constant.NEW_LINE);
				}
				stringBuffer.append("}");
			}
			if (_catches != null) {
				for (CatClause catClause : _catches) {
					tmp = catClause.transfer(vars, exprMap, retType, exceptions, metric);
					if (tmp == null) return null;
					stringBuffer.append(tmp);
				}
			}
			if (_finallyBlk != null) {
				stringBuffer.append("finally");
				tmp = _finallyBlk.transfer(vars, exprMap, retType, exceptions, metric);
				if (tmp == null) return null;
				stringBuffer.append(tmp);
			}
		}
		return stringBuffer;
	}

	@Override
	public StringBuffer transfer(VarScope vars, Map<String, String> exprMap, String retType, Set<String> exceptions,
                                 Adaptee metric) {
		StringBuffer stringBuffer = super.transfer(vars, exprMap, retType, exceptions, metric);
		if (stringBuffer == null) {
			return transfer(vars, exprMap, retType, exceptions, null, metric);
		}
		return stringBuffer;
	}

	@Override
	public StringBuffer adaptModifications(VarScope vars, Map<String, String> exprMap, String retType,
                                           Set<String> exceptions, Adaptee metric) {
		Node pnode = NodeUtils.checkModification(this);
		if (pnode != null) {
			TryStmt tryStmt = (TryStmt) pnode;
			List<Modification> catchModifications = new LinkedList<>();
			StringBuffer finallyBlock = null;
			for (Modification modification : tryStmt.getModifications()) {
				if (modification instanceof Update) {
					Update update = (Update) modification;
					Node node = update.getSrcNode();
					if (node == tryStmt._finallyBlk) {
						finallyBlock = update.apply(vars, exprMap, retType, exceptions, metric);
						if (finallyBlock == null) return null;
					} else {
						catchModifications.add(modification);
					}
				} else {
					catchModifications.add(modification);
				}
			}

			StringBuffer stringBuffer = new StringBuffer("try");
			StringBuffer tmp;
			if (_resource != null && _resource.size() > 0) {
				stringBuffer.append("(");
				stringBuffer.append(_resource.get(0).toSrcString());
				for (int i = 1; i < _resource.size(); i++) {
					stringBuffer.append(";");
					stringBuffer.append(_resource.get(i).toSrcString());
				}
				stringBuffer.append(")");
			}

			tmp = _blk.adaptModifications(vars, exprMap, retType, exceptions, metric);
			if (tmp == null) return null;
			stringBuffer.append(tmp);


			if (_catches != null && _catches.size() > 0) {
				if (catchModifications.size() > 0) {
					Map<Node, List<StringBuffer>> insertionBefore = new HashMap<>();
					Map<Node, List<StringBuffer>> insertionAfter = new HashMap<>();
					Map<Integer, List<StringBuffer>> insertionAt = new HashMap<>();
					Map<Node, StringBuffer> map = new HashMap<>(_catches.size());

					if (!Matcher.applyNodeListModifications(catchModifications, _catches, insertionBefore,
							insertionAfter, insertionAt, map, vars, exprMap, retType, exceptions, metric)) {
						return null;
					}

					tmp = NodeUtils.assemble(_catches, insertionBefore, insertionAfter, map, insertionAt
							, vars, exprMap, retType, exceptions, metric);
					if (tmp == null) return null;
					stringBuffer.append(tmp);
				} else {
					for (CatClause catClause : _catches) {
						tmp = catClause.adaptModifications(vars, exprMap, retType, exceptions, metric);
						if (tmp == null) return null;
						stringBuffer.append(tmp);
					}
				}
			}
			if (finallyBlock == null) {
				if (_finallyBlk != null) {
					stringBuffer.append("finally");
					tmp = _finallyBlk.adaptModifications(vars, exprMap, retType, exceptions, metric);
					if (tmp == null) return null;
					stringBuffer.append(tmp);
				}
			} else {
				stringBuffer.append("finally");
				stringBuffer.append(finallyBlock);
			}
			return stringBuffer;

		} else {
			StringBuffer stringBuffer = new StringBuffer("try");
			StringBuffer tmp;
			if (_resource != null && _resource.size() > 0) {
				stringBuffer.append("(");
				tmp = _resource.get(0).adaptModifications(vars, exprMap, retType, exceptions, metric);
				if (tmp == null) return null;
				stringBuffer.append(tmp);
				for (int i = 1; i < _resource.size(); i++) {
					stringBuffer.append(";");
					tmp = _resource.get(i).adaptModifications(vars, exprMap, retType, exceptions, metric);
					if (tmp == null) return null;
					stringBuffer.append(tmp);
				}
				stringBuffer.append(")");
			}
			tmp = _blk.adaptModifications(vars, exprMap,retType, exceptions, metric);
			if (tmp == null) return null;
			stringBuffer.append(tmp);
			if (_catches != null) {
				for (CatClause catClause : _catches) {
					tmp = catClause.adaptModifications(vars, exprMap, retType, exceptions, metric);
					if (tmp == null) return null;
					stringBuffer.append(tmp);
				}
			}
			if (_finallyBlk != null) {
				stringBuffer.append("finally");
				tmp = _finallyBlk.adaptModifications(vars, exprMap, retType, exceptions, metric);
				if (tmp == null) return null;
				stringBuffer.append(tmp);
			}
			return stringBuffer;
		}
	}
}
