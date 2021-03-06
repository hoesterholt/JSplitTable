/* ******************************************************************************
 *
 *       Copyright 2008-2010 Hans Dijkema
 *       This file is part of the JDesktop SwingX library
 *       and part of the SwingLabs project
 *
 *   SwingX is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as 
 *   published by the Free Software Foundation, either version 3 of 
 *   the License, or (at your option) any later version.
 *
 *   SwingX is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with SwingX.  If not, see <http://www.gnu.org/licenses/>.
 *   
 * ******************************************************************************/

package net.dijkema.splittable;

import java.util.Hashtable;

/**
 * This model must implement a two level model consisting of nodes with rows per node.
 * 
 * @author Hans Dijkema
 *
 */
public abstract class AbstractTwoLevelSplitTableModel extends AbstractSplitTableModel {

	private static final long serialVersionUID = 1L;


	public class NoValue {
		private String _msg;
		
		public String toString() {
			return _msg;
		}
		
		public NoValue(String m) {
			_msg=m;
		}
	}
	
	public class CNodeIndex {
		public int nodeIndex;
		public int nodeRow;
		public int column;
		
		public int getNodeIndex() {
			return nodeIndex;
		}
		
		public int getNodeRow() {
			return nodeRow;
		}
		
		public int getColumn() {
			return column;
		}

		public String toString() {
			return "nodeIndex="+nodeIndex+", nodeRow="+nodeRow+", column="+column;
		}
		
		public void set(int ni,int nr,int c) {
			nodeIndex=ni;nodeRow=nr;column=c;
		}
		
		public boolean isValid() {
			return nodeIndex>=0 && column>=0;
		}
		
		public boolean isValidValue() {
			return nodeIndex >= 0 && nodeRow >= 0 && column >= 0;
		}
		
		public CNodeIndex(int ni,int nr,int c) {
			nodeIndex=ni;
			nodeRow=nr;
			column=c;
		}

		public void setColumn(int col) {
			column = col;
		}
	}
	
	private NoValue noval;
	
	public AbstractTwoLevelSplitTableModel() {
		noval=new NoValue("");
	}
	
	/**
	 * Must return the value of the node at nodeIndex and node Column.
	 * 
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public abstract Object 		getNodeValue(int nodeIndex,int nodeColumn);
	
	/**
	 * Must return the number of columns in a node at the node level.
	 * getColumnCount must return it for the number of columns contained in each node.
	 * 
	 * @return
	 */
	public abstract int         getNodeColumnCount();
	
	/**
	 * Must return the number of nodes in this model.
	 * @return
	 */
	public abstract int         getNodeRowCount();
	
	/**
	 * Must return the number of rows contained in the specific node. When the
	 * node gets expanded, all rows in the node are displayed.
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public abstract int    		getNodeRowCount(int nodeIndex);
	
	/**
	 * Returns the column count for a given node index. Normally this should be
	 * equal for all nodes, however, it can be different. The last columns will
	 * not be painted for those nodes, for the table will contain a number of 
	 * columns equal to the max of all NodeColumnCounts(). NB. If a node column
	 * count for a node changes or a new node is inserted with a different node
	 * column count, fireModelDataChanged() must be called.
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public abstract int 		getNodeColumnCount(int nodeIndex);
	
	/**
	 * Must return the 'expanded' state of the node. True: expanded, false, not expanded.
	 * If a model changes the expanded state, outside the control of the Jzc3TwoLevelSplitTable() 
	 * it must fire a fireModelDataChanged().
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public abstract boolean  getNodeExpanded(int nodeIndex);
	
	
	/**
	 * Is used by the Jzc3TwoLevelSplitTable() to inform the model of a expansion change.
	 * The result of setNodeExpanded informs the Jzc3TwoLevelSplitTable() of the actual result
	 * of the expansion request.
	 * 
	 * @param nodeIndex
	 * @return
	 */
	public abstract boolean  setNodeExpanded(int nodeIndex,boolean true_is_yes);
	
	
	/**
	 * Must return the value of the nodeRow contained in the node with nodeIndex at nodeColumn. 
	 */
	public abstract Object  getValueAt(int nodeIndex,int nodeRow,int nodeColumn);
	
	/**
	 * Has a default implementation of returning false. Override, if you want nodes
	 * to be editable.
	 * @param nodeIndex
	 * @param nodeColumn
	 * @return
	 */
	public boolean		isNodeEditable(int nodeIndex,int nodeColumn) {
		return false;
	}
	
	/**
	 * Has a default implementation of returning false. Override if you want cells
	 * to be editable.
	 * @param nodeIndex
	 * @param nodeRow
	 * @param nodeColumn
	 * @return
	 */
	public boolean		isCellEditable(int nodeIndex,int nodeRow,int nodeColumn) {
		return false;
	}
	
	/**
	 * Has a default implementation which does nothing.
	 * @param val
	 * @param nodeIndex
	 * @param nodeRow
	 * @param nodeColumn
	 */
	public void setValueAt(Object val,int nodeIndex,int nodeRow,int nodeColumn) {
		// does nothing
	}
	
	
	/**
	 * Has a default implementation which does nothing
	 * @param val
	 * @param nodeIndex
	 * @param nodeColumn
	 */
	public void setNodeValueAt(Object val,int nodeIndex,int nodeColumn) {
		// does nothing
	}
	
	
	

	////////////////////////////////////////////////////////////////////////////////////////////
	// Het echte werk
	////////////////////////////////////////////////////////////////////////////////////////////
	
	private int _maxcolumns=-1;
	
	final public int getColumnCount() {
		if (_maxcolumns==-1) {
			_maxcolumns=this.getNodeColumnCount();
			int i,N;
			for(i=0,N=this.getNodeRowCount();i<N;i++) {
				if (this.getNodeColumnCount(i)>_maxcolumns) {
					_maxcolumns=this.getNodeColumnCount(i);
				}
			}
		}
		return _maxcolumns;
	}
	
	public void fireTableStructureChanged() {
		_maxcolumns=-1;
		_cnode_index_hash.clear();
		super.fireTableStructureChanged();
	}

	public void fireTableDataChanged() {
		_cnode_index_hash.clear();
		super.fireTableDataChanged();
	}
	
	final public int getRowCount() {
		int i,N;
		N=0;
		for(i=0;i<this.getNodeRowCount();i++) {
			N+=1;
			if (this.getNodeExpanded(i)) {
				N += this.getNodeRowCount(i);
			}
		}
		return N;
	}
	
	private Hashtable<Integer, CNodeIndex> _cnode_index_hash = new Hashtable<Integer, CNodeIndex>();
	private CNodeIndex _two_index = new CNodeIndex(-2, -2, -2);
	private CNodeIndex _three_index=new CNodeIndex(-3, -3, -3);

	private void putCNodeIndex(int row, int col, CNodeIndex i) {
		_cnode_index_hash.put(row, i);
	}
	
	final public CNodeIndex getCNodeIndex(int row, int col) {
		CNodeIndex ind = _cnode_index_hash.get(row);
		
		if (ind != null) {
			ind.setColumn(col);
			return ind; 
		} else {
			int orig_row = row;
			int orig_col = col;
			int nodeIndex=0;
			int origRow=row;
			boolean goOn=true;
			CNodeIndex _cnode_index = new CNodeIndex(-3, -3 , -3);
			while (goOn) {
				
				if (row==0) {
					// We zijn *op* een node beland.
					_cnode_index.set(nodeIndex, -1, col);
					putCNodeIndex(orig_row, orig_col, _cnode_index);
					return _cnode_index;
				} 
				
				if (this.getNodeExpanded(nodeIndex)) {
					int rrow = row - 1;
					int r = this.getNodeRowCount(nodeIndex);
					if (rrow<r) {
						// We zijn *in* een node beland
						_cnode_index.set(nodeIndex,rrow,col);
						putCNodeIndex(orig_row, orig_col, _cnode_index);
						return _cnode_index;
					} else {
						// We moeten deze node met inhoud tellen
						row -= this.getNodeRowCount(nodeIndex);
						nodeIndex += 1;
						row -= 1;
					}
				} else {
					// we moeten deze node tellen, maar zonder inhoud
					row-=1;
					nodeIndex+=1;
				}
				
				// Stop criterium, maar dan is er ook iets mis!
				
				if (row<0) {
					goOn=false;
					try {
						throw new AbstractTwoLevelSplitModelException("Row count drops below 0 while calculating nodeIndex/nodeRow position");
					} catch (Exception E) {
						//logger.error(E);
						E.printStackTrace();
					}
					_cnode_index.set(-1,-1,-1);
					return _cnode_index;
				}
			}
			
			_cnode_index.set(-2,-2,-2);
			putCNodeIndex(orig_row, orig_col, _cnode_index);
			return _cnode_index;
		}
	}
	
	private String _blowZero=new String("RowBelowZero");
	private String _unreach=new String("Unreachable");
	
	final public boolean isNodeIndex(int row) {
		CNodeIndex cnode=getCNodeIndex(row, 0);
		
		if (cnode.nodeIndex<0) {
			return false;
		} else if (cnode.nodeRow==-1) {
			return true;
		} else {
			return false;
		}
	}
	
	final public int getNodeIndex(int row) {
		CNodeIndex cnode=getCNodeIndex(row,0);
		return cnode.nodeIndex;
	}
	
	final public Object getValueAt(int row,int col) {
		CNodeIndex cnode = getCNodeIndex(row, col);
		
		if (cnode.nodeIndex==-1) {
			return _blowZero;
		} else if (cnode.nodeIndex==-2) {
			return _unreach;
		} else if (cnode.nodeRow==-1) {
			if (cnode.column>=this.getNodeColumnCount()) {
				return noval;
			} else {
				return this.getNodeValue(cnode.nodeIndex, cnode.column);
			}
		} else { 
			if (col >= this.getNodeColumnCount(cnode.nodeIndex)) {
				return noval;
			} else {
				return this.getValueAt(cnode.nodeIndex, cnode.nodeRow, cnode.column);
			}
		}
	}

	final public void setValueAt(Object val,int row,int col) {
		CNodeIndex cnode=getCNodeIndex(row,col);
		
		if (cnode.nodeIndex==-1) {
			// do nothing
		} else if (cnode.nodeIndex==-2) {
			// do nothing
		} else if (cnode.nodeRow==-1) {
			if (cnode.column>=this.getNodeColumnCount()) {
				// do nothing
			} else {
				this.setNodeValueAt(val,cnode.nodeIndex, cnode.column);
			}
		} else { 
			if (col>=this.getNodeColumnCount(cnode.nodeIndex)) {
				// do nothing
			} else {
				this.setValueAt(val,cnode.nodeIndex, cnode.nodeRow,cnode.column);
			}
		}
	}
	
	
	final public boolean isCellEditable(int row,int col) {
		CNodeIndex cnode=getCNodeIndex(row,col);
		
		if (cnode.nodeIndex==-1) {
			return false;
		} else if (cnode.nodeIndex==-2) {
			return false;
		} else if (cnode.nodeRow==-1) {
			if (cnode.column>=this.getNodeColumnCount()) {
				return false;
			} else {
				return this.isNodeEditable(cnode.nodeIndex,cnode.column);
			}
		} else { 
			if (col>=this.getNodeColumnCount(cnode.nodeIndex)) {
				return false;
			} else {
				return this.isCellEditable(cnode.nodeIndex, cnode.nodeRow,cnode.column);
			}
		}
		
	}
	
}
