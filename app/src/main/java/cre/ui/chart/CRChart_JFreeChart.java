package cre.ui.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import javafx.scene.Node;
import cre.CRELogger;
import cre.data.type.abs.CRChartData;
import cre.data.type.abs.CRChartData.SERIESTYPE;
import cre.data.type.abs.Statistics.IntRange;
import cre.ui.UISettings;

public abstract class CRChart_JFreeChart extends CRChart {

	private JFreeChart chart;
	private ChartViewer chView;
	 
	private DefaultXYDataset ds;

	private boolean duringUpdate;
	
	public CRChart_JFreeChart () {

		super();
		duringUpdate = false;
		
		ds = new DefaultXYDataset();
		chart = ChartFactory.createXYLineChart("", CRChart.xAxisLabel, CRChart.yAxisLabel, ds);

		chart.getLegend().setFrame(BlockBorder.NONE);
		
		XYPlot plot = chart.getXYPlot();
		plot.setRangeZeroBaselineVisible(true);
		plot.setRangeZeroBaselinePaint(Color.black);
		plot.setRangeZeroBaselineStroke(new BasicStroke(2));
		plot.setRenderer(new XYSplineRenderer());
		
		// domain=year -> show year number always with 4 digits
		NumberAxis dAxis = new NumberAxis();
		dAxis.setNumberFormatOverride(new DecimalFormat("0000.#"));
		dAxis.setLabel(CRChart.xAxisLabel);
		plot.setDomainAxis (dAxis);
		
		// general chart layout
		XYPlot rPlot = plot.getRenderer().getPlot();
		rPlot.setBackgroundPaint(Color.white);
		rPlot.setOutlinePaint(Color.black);
		rPlot.setDomainGridlinePaint(Color.gray);
		rPlot.setRangeGridlinePaint(Color.gray);
		
		// layout for data rows
		XYItemRenderer rend = plot.getRenderer();
//		double shapeSize = 6;	// 6
//		float strokeSize = 3;	// 3
//		rend.setSeriesShape(0, new Rectangle2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));
//		rend.setSeriesStroke(0, new BasicStroke(strokeSize));
//		rend.setSeriesShape(1, new Ellipse2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));
//		rend.setSeriesStroke(1, new BasicStroke(strokeSize));
		setFontSize();
		
		
	
		
		
		// tooltip = year + CR + difference to median
		XYToolTipGenerator tooltip = new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				return String.format("%d\n* %s: %d\n* %s: %s", 
						ds.getX(0, item).intValue(), 
						getSeriesLabel(0), 
						ds.getY(0, item).intValue(),
						getSeriesLabel(1),
						ds.getSeriesCount()>1 ? ds.getY(1, item).intValue() : "");
			}
		};
		rend.setSeriesToolTipGenerator(0, tooltip);
		rend.setSeriesToolTipGenerator(1, tooltip);
		
		
//		rend.setSeriesToolTipGenerator(0, new XYToolTipGenerator() {
//			@Override
//			public String generateToolTip(XYDataset dataset, int series, int item) {
//				return String.format("Year=%d\n, %s=%d", dataset.getX(0, item).intValue(), series==0 ? "N_CR" : "Diff", dataset.getY(series, item).intValue());
//			}
//		});
		
		// update table when zoom changes in chart
		plot.addChangeListener(pcevent -> {
			if (!duringUpdate) {
				CRELogger.get().logInfo("onChange");
				CRELogger.get().logInfo(String.valueOf(dAxis.getLowerBound()));
				CRELogger.get().logInfo(String.valueOf(dAxis.getUpperBound()));
				onYearRangeFilter(dAxis.getLowerBound(), dAxis.getUpperBound());
			}
		});
		

		chView = new ChartViewer(chart);
		chView.addChartMouseListener(new ChartMouseListenerFX() {
			
			@Override
			public void chartMouseMoved(ChartMouseEventFX arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEventFX cmevent) {
				if (cmevent.getEntity() instanceof XYItemEntity) {
					/* get year (domain value) of clicked data item */
					XYItemEntity a = (XYItemEntity) cmevent.getEntity();
					int year = a.getDataset().getX(a.getSeriesIndex(), a.getItem()).intValue();
					onSelectYear (year);
				}	
			}
		});
	}


	
	@Override
	public Node getNode() {
		return chView;
	}

	@Override
	public void setVisible (boolean value) {
		chView.setVisible(value);
	}

	@Override
	public boolean isVisible () {
		return chView.isVisible();
	}

	@Override
	protected void setChartDomainRange(IntRange range) {
		org.jfree.data.Range dAxisRange = chart.getXYPlot().getDomainAxis().getRange();
//		if ((((int)Math.ceil (dAxisRange.getLowerBound())) != range[0]) || (((int)Math.floor(dAxisRange.getUpperBound())) != range[1])) { 
			CRELogger.get().logInfo("Adjusting");
			CRELogger.get().logInfo("Axis = " + dAxisRange.toString());
			CRELogger.get().logInfo("Year = " + range.getMin() + ", " + range.getMax());
			duringUpdate = true;
			if (range.getMin()==range.getMax()) {
				chart.getXYPlot().getDomainAxis().setRange(range.getMin()-0.5, range.getMax()+0.5);
			} else {
				chart.getXYPlot().getDomainAxis().setRange(range.getMin(), range.getMax());
			}
			duringUpdate = false;

//		}
	}

	@Override
	public void updateData(CRChartData data) {
		
		this.duringUpdate = true;	// avoids triggering plot change listener
		
		// delete previous chart lines
		while (ds.getSeriesCount()>0) {
			ds.removeSeries(ds.getSeriesKey(ds.getSeriesCount()-1));
		}
		
		// generate chart lines
		if (UISettings.get().getChartLine()[0]) {
			ds.addSeries(getSeriesLabel(0), new double[][] { 
				Arrays.stream(data.getRPY()).asDoubleStream().toArray(), 
				Arrays.stream(data.getSeries(SERIESTYPE.NCR)).asDoubleStream().toArray()
			});
		}
		
		if (UISettings.get().getChartLine()[1]) {
			ds.addSeries(getSeriesLabel(1), new double[][] { 
				Arrays.stream(data.getRPY()).asDoubleStream().toArray(), 
				Arrays.stream(data.getSeries(SERIESTYPE.MEDIANDIFF)).asDoubleStream().toArray()
			});
		}
		
		this.duringUpdate = false;
	}


	
	@Override
	public void setFontSize() {
		
		XYItemRenderer rend = chart.getXYPlot().getRenderer();
		float strokeSize = UISettings.get().getChartSize()[0];;	// 3
		double shapeSize = UISettings.get().getChartSize()[1];	// 6
		
		rend.setSeriesShape(0, new Rectangle2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));
		rend.setSeriesStroke(0, new BasicStroke(strokeSize));
		rend.setSeriesShape(1, new Ellipse2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));
		rend.setSeriesStroke(1, new BasicStroke(strokeSize));
		
		
		XYPlot plot = chart.getXYPlot();
		plot.getRangeAxis(0).setLabelFont(new Font(null, Font.PLAIN, UISettings.get().getChartSize()[2]));
		plot.getRangeAxis(0).setTickLabelFont(new Font(null, Font.PLAIN, UISettings.get().getChartSize()[3]));

		plot.getDomainAxis(0).setLabelFont(new Font(null, Font.PLAIN, UISettings.get().getChartSize()[2]));
		plot.getDomainAxis(0).setTickLabelFont(new Font(null, Font.PLAIN, UISettings.get().getChartSize()[3]));
		
//		rend.setSeriesItemLabelFont(0, new Font(null, Font.PLAIN, UserSettings.get().getChartSize()[2]));
//		rend.setSeriesItemLabelFont(1, new Font(null, Font.PLAIN, UserSettings.get().getChartSize()[2]));
		
//		plot.getDomainAxis(0).setLabelFont(font3);
//		plot.getDomainAxis(0).setTickLabelFont(font3);
		for (int i=0; i<plot.getLegendItems().getItemCount(); i++) {
			plot.getLegendItems().get(i).setLabelFont(new Font(null, Font.PLAIN, UISettings.get().getChartSize()[3]));
		}
		
		
	}
	
	@Override
	public void autoRange() {
		XYPlot plot = chart.getXYPlot();
		if (plot.getDataRange(plot.getDomainAxis()).getLength()==0) return;
		
		plot.getDomainAxis().setRange (Range.expand(plot.getDataRange(plot.getDomainAxis()), 0.025, 0.025));
		plot.getRangeAxis().setRange (Range.expand(plot.getDataRange(plot.getRangeAxis()), 0.025, 0.025));
	}
	
}
