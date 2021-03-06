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

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;



abstract public class AbstractSplitTableModel extends AbstractTableModel {
	
	//static Logger logger=zc3Logger.getLogger(AbstractSplitTableModel.class);

	private static final long serialVersionUID = 1L;
	
	public interface ColumnsListener {
		public void numOfColumnsChanged(AbstractSplitTableModel m);
	}
	
	class SplitModel extends AbstractTableModel {
		
		private int _col_count = -1;
		private int _row_count = -1;
		private Vector<String> _col_names = new Vector<String>();
		private Vector<Class<?>> _col_classes = new Vector<Class<?>>();
		private boolean _init = true;
		
		private static final long serialVersionUID = 1L;
		
		private boolean 					_left;
		private AbstractSplitTableModel 	_model;
		
		public SplitModel(AbstractSplitTableModel m,boolean left) {
			_left = left;
			_model = m;
			_init = true;
		}

		private void init() {
			_init = false;
			
			if (_model == null) {
				_col_count = 0;
				_row_count = 0;
				_col_names.clear();
				_col_classes.clear();
				return;
			}
			
			// column count
			if (_left) {
				_col_count = _model.getSplitColumn();
			} else {
				_col_count = _model.getColumnCount() - _model.getSplitColumn();
			}
			
			// row count
			_row_count = _model.getRowCount();
			
			// Column names
			{
				_col_names.clear();
				int col, N;
				for(col = 0, N = _col_count; col < N; col++) {
					String name;
					if (_left) {
						name = _model.getColumnName(col);
					} else {
						name = _model.getColumnName(_model.getSplitColumn() + col);
					}
					_col_names.add(name);
				}
			}
			
			// Column classes
			{
				_col_classes.clear();
				int col, N;
				for(col = 0, N = _col_count; col < N; col++) {
					Class<?> c = _model.getColumnClass(col);
					_col_classes.add(c);
				}
			}
			
		}
		
		
		public int getColumnCount() {
			if (_init) init();
			return _col_count;
		}

		public int getRowCount() {
			if (_init) init();
			return _row_count;
		}

		public Object getValueAt(int row, int col) {
			if (_left) {
				return _model.getValueAt(row,col);
			} else {
				return _model.getValueAt(row, _model.getSplitColumn() + col);
			}
		}
		
		public String getColumnName(int col) {
			if (_init) init();
			return _col_names.get(col);
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			if (_init) init();
			return _col_classes.get(columnIndex);
		}
		
		public boolean isCellEditable(int row,int col) {
			if (_left) {
				return _model.isCellEditable(row,col);
			} else {
				return _model.isCellEditable(row, _model.getSplitColumn()+col);
			}
		}
		
		public void setValueAt(Object val,int row,int col) {
			if (_left) {
				_model.setValueAt(val,row,col);
			} else {
				_model.setValueAt(val,row, _model.getSplitColumn()+col);
			}
		}
		
		public void fireTableDataChanged() {
			_init = true;
			super.fireTableDataChanged();
		}
		
		public void fireTableStructureChanged() {
			_init = true;
			super.fireTableStructureChanged();
		}
		
	}
	
	
	public AbstractSplitTableModel() {
		_left = new SplitModel(this,true);
		_right = new SplitModel(this,false);	
	}
	
	private AbstractTableModel _left;
	private AbstractTableModel _right;
	
	public AbstractTableModel modelLeft() {
		return _left;
	}
	
	public AbstractTableModel modelRight() {
		return _right;
	}
	
	public interface ColumnWidthListener {
		public void prefferedWidthForColumn(int col,int width);
	}
	
	private Set<ColumnWidthListener> _listeners=new HashSet<ColumnWidthListener>();  
	
	public void addColumnWidthListener(ColumnWidthListener l) {
		_listeners.add(l);
		informListener(l);
	}
	
	public void removeColumnWidthListener(ColumnWidthListener l) {
		_listeners.remove(l);
	}
	
	private Set<ColumnsListener> _clisteners = new HashSet<ColumnsListener>();
	
	public void addColumnsListener(ColumnsListener l) {
		_clisteners.add(l);
	}
	
	public void removeColumnsListener(ColumnsListener l) {
		_clisteners.remove(l);
	}
	
	protected void informColumnsChange() {
		Iterator<ColumnsListener> it = _clisteners.iterator();
		while(it.hasNext()) {
			it.next().numOfColumnsChanged(this);
		}
	}
	
	private SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Date             ctime=new Date();
	
	//private void logtime(String msg) {
	//	ctime.setTime(System.currentTimeMillis());
	//	logger.debug(format.format(ctime)+" "+msg);
	//}

	private JLabel _label=new JLabel(" ");
	
	private void informListener(ColumnWidthListener l) {
		int i,N;
		//logtime("Calc widths B ");
		for(i=0,N=this.getColumnCount();i<N;i++) {
			String s=this.getMaxString(i);
			if (s!=null) {
				_label.setText(s);
				Dimension d=_label.getPreferredSize();
				int width=d.width;
				l.prefferedWidthForColumn(i, width);
			}
		}
		//logtime("Calc widths E ");
	}
	
	private JLabel q=new JLabel();
	
	public void informListeners() {
		int i,N;
		for(i=0,N=this.getColumnCount();i<N;i++) {
			String s=this.getMaxString(i);
			if (s!=null) {
				q.setText(s);
				Dimension d=q.getPreferredSize();
				int width=d.width;
				Iterator<ColumnWidthListener> it=_listeners.iterator();
				while (it.hasNext()) {
					ColumnWidthListener c=it.next();
					if (c!=null) {
						c.prefferedWidthForColumn(i, width);
					}
				}
			}
		}
	}
	
	/**
	 * Voor intern gebruik.
	 * 
	 * @param lm
	 * @param rm
	 */
	private void setModels(AbstractTableModel lm,AbstractTableModel rm) {
		_left=lm;
		_right=rm;
	}

	/**
	 * Moet de colom terug geven waar moet worden gedeeld.
	 * 
	 * @return
	 */
	abstract public int getSplitColumn();
	
	
	/** 
	 * Geef een string met maximale lengte terug, om de breedte van een 
	 * colom te bepalen. null ==> er is geen maximale lengte om te gebruiken.
	 *   
	 * @param col
	 * @return
	 */
	public String getMaxString(int col) {
		return null;
	}
	
	public void fireTableDataChanged() {
		if (_left!=null) {
			_left.fireTableDataChanged();
			_right.fireTableDataChanged();
			this.informColumnsChange();
		}
	}
	
	public void fireTableChanged(TableModelEvent e) {
		if (_left!=null) {
			_left.fireTableChanged(e);
			_right.fireTableChanged(e);
			this.informColumnsChange();
		}
	}
	
	public void fireTableCellUpdated(int row,int column) {
		if (_left!=null) {
			_left.fireTableCellUpdated(row, column);
			_right.fireTableCellUpdated(row, column);
		}
	}
	
	public void fireTableRowsDeleted(int firstRow,int lastRow) {
		if (_left!=null) {
			_left.fireTableRowsDeleted(firstRow, lastRow);
			_right.fireTableRowsDeleted(firstRow, lastRow);
		}
	}
	
	public void fireTableRowsInserted(int firstRow,int lastRow) {
		if (_left!=null) {
			_left.fireTableRowsInserted(firstRow, lastRow);
			_right.fireTableRowsInserted(firstRow, lastRow);
		}
	}
	
	public void fireTableRowsUpdated(int firstRow,int lastRow) {
		if (_left!=null) {
			_left.fireTableRowsUpdated(firstRow, lastRow);
			_right.fireTableRowsUpdated(firstRow, lastRow);
		}
	}
	
	public void fireTableStructureChanged() {
		if (_left!=null) {
			_left.fireTableStructureChanged();
			_right.fireTableStructureChanged();
			informListeners();
			this.informColumnsChange();
		}
	}
	
	public TableModelListener[] getTableModelListeners() {
		return _right.getTableModelListeners();
	}
}
