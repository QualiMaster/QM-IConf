package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.widgets.Display;

/**
 * A rounded rectangle figure containing a label at the top and, below it, an 
 * embedded TextFlow within a FlowPage that contains text.
 */
public class CustomTextFigure extends RoundedRectangle {
    
    private static final Dimension CORNER_DIMENSIONS = new Dimension(10, 10);
    private static final int BORDER_LINE_WIDTH = 2;
    private static final int DEFAULT_BORDER_WIDTH = 7;
    private Label topLabel;
    private FlowPage flowPage;   
    private TextFlow textFlow;
        
    /**
     * Creates a new TextNoteFigure with a default border size.
     */
    public CustomTextFigure() {
         this(DEFAULT_BORDER_WIDTH);
    }

    /**
     * Creates a new TextNoteFigure with a MarginBorder that is the given size and
     * a FlowPage containing a TextFlow with the style WORD_WRAP_SOFT.
     * 
     * @param borderSize
     *            the size of the MarginBorder
     */
    public CustomTextFigure(int borderSize) {
        setCornerDimensions(CORNER_DIMENSIONS);
        setLineWidth(BORDER_LINE_WIDTH);
        setFont(Display.getCurrent().getSystemFont());
        setBorder(new MarginBorder(borderSize));
        flowPage = new FlowPage();

        topLabel = new Label();
        
        textFlow = new TextFlow();
        textFlow.setForegroundColor(ColorConstants.black);

        textFlow.setLayoutManager(new ParagraphTextLayout(textFlow,
                        ParagraphTextLayout.WORD_WRAP_SOFT));
        
        flowPage.add(textFlow);

        BorderLayout layout = new BorderLayout();
        setLayoutManager(layout);
        add(topLabel);
        add(flowPage);
        layout.setVerticalSpacing(3);
        layout.setConstraint(topLabel, BorderLayout.TOP);
        layout.setConstraint(flowPage, BorderLayout.CENTER);
    }

    /**
     * Returns the text inside the TextFlow.
     * 
     * @return the text flow inside the text.
     */
    public String getText() {
        return textFlow.getText();
    }

    /**
     * Sets the text of the TextFlow to the given value.
     * 
     * @param newText
     *            the new text value.
     */
    public void setText(String newText) {
        textFlow.setText(newText);
    }

    /**
     * Set the text for the label above the text box.
     * @param newText New text for the label.
     */
    public void setTopLabelText(String newText) {
        topLabel.setText(newText);
    }
    
    /**
     * Get the boundaries of the editable text area (in parent coordinates).
     * @return Rectangle giving the boundaries of the editable text area.
     */
    public Rectangle getEditableArea() {
        Rectangle area = flowPage.getClientArea();
        flowPage.translateToParent(area);
        return area;
    }
}