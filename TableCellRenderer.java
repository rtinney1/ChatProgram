import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

public class TableCellRenderer extends DefaultTableCellRenderer
{
	String nameToFind;
	Color colorToChangeTo;

	public TableCellRenderer(String name, Color colorWanted)
	{
		nameToFind = name;
		colorToChangeTo = colorWanted;
	}

  	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  	{
		DefaultTableModel myTableModel;

    	Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		myTableModel = (DefaultTableModel)table.getModel();

   		if (column == 0)
   		{
   		    if(myTableModel.getValueAt(row, 0).equals(nameToFind))
   		    {
      			c.setForeground(colorToChangeTo);
      		}
			else
			{
				c.setForeground(Color.BLUE);
			}
		}

		return c;
  	}
}