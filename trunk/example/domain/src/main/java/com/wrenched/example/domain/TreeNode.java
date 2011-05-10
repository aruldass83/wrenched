package com.wrenched.example.domain;

import java.util.ArrayList;
import java.util.List;

import com.wrenched.core.annotations.Externalizable;
import com.wrenched.example.tree.NodeType;

@Externalizable
public class TreeNode {
	private Integer id;
	private List<TreeNode> children;
	private TreeNode parent;
	
	private transient final NodeType model;
	
	public TreeNode() {
		model = null;
	}
	
	public TreeNode(NodeType m) {
		this.model = m;
		this.setId(Integer.valueOf(this.model.getId()));
		
		this.children = (m.getChildren() == null) ? null : new ArrayList<TreeNode>();
	}
	
	public NodeType getModel() {
		return model;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public List<TreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}
	public void addChild(TreeNode child) {
		this.children.add(child);
	}
	
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
}