package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.entity.Order;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalesReportPdfService {

    private static final Color DARK_HEADER = new Color(33, 37, 41);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color BORDER_COLOR = new Color(180, 180, 180);

    private final DecimalFormat moneyFormat =
            new DecimalFormat("#,##0.00");

    public byte[] generateReport(
            LocalDate from,
            LocalDate to,
            BigDecimal totalRevenue,
            long totalOrders,
            long productsSold,
            BigDecimal averageOrder,
            List<Order> orders
    ) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        // Landscape gives the table more room.
        Document document = new Document(
                PageSize.A4.rotate(),
                30,
                30,
                40,
                45
        );

        try {
            PdfWriter writer =
                    PdfWriter.getInstance(document, outputStream);

            writer.setPageEvent(new ReportPageEvent());

            document.open();

            Font storeFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    22,
                    Color.BLACK
            );

            Font reportTitleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    15,
                    new Color(70, 70, 70)
            );

            Font normalFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    10,
                    Color.BLACK
            );

            Font summaryLabelFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    10,
                    Color.DARK_GRAY
            );

            Font summaryValueFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    13,
                    Color.BLACK
            );

            Font tableHeaderFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    9,
                    Color.WHITE
            );

            Font tableFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    8,
                    Color.BLACK
            );

            // Store name
            Paragraph storeName = new Paragraph(
                    "DABE TechStore",
                    storeFont
            );
            storeName.setAlignment(Element.ALIGN_CENTER);
            document.add(storeName);

            // Report title
            Paragraph reportTitle = new Paragraph(
                    "SALES REPORT",
                    reportTitleFont
            );
            reportTitle.setAlignment(Element.ALIGN_CENTER);
            reportTitle.setSpacingAfter(4);
            document.add(reportTitle);

            DateTimeFormatter dateFormatter =
                    DateTimeFormatter.ofPattern("MMM d, yyyy");

            Paragraph period = new Paragraph(
                    "Report Period: "
                            + from.format(dateFormatter)
                            + " - "
                            + to.format(dateFormatter),
                    normalFont
            );
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(3);
            document.add(period);

            DateTimeFormatter generatedFormatter =
                    DateTimeFormatter.ofPattern(
                            "MMM d, yyyy h:mm a"
                    );

            Paragraph generated = new Paragraph(
                    "Generated: "
                            + LocalDateTime.now()
                            .format(generatedFormatter),
                    normalFont
            );
            generated.setAlignment(Element.ALIGN_CENTER);
            generated.setSpacingAfter(18);
            document.add(generated);

            // Summary table
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(18);

            addSummaryCell(
                    summaryTable,
                    "TOTAL REVENUE",
                    "PHP " + formatMoney(totalRevenue),
                    summaryLabelFont,
                    summaryValueFont
            );

            addSummaryCell(
                    summaryTable,
                    "TOTAL ORDERS",
                    String.valueOf(totalOrders),
                    summaryLabelFont,
                    summaryValueFont
            );

            addSummaryCell(
                    summaryTable,
                    "PRODUCTS SOLD",
                    String.valueOf(productsSold),
                    summaryLabelFont,
                    summaryValueFont
            );

            addSummaryCell(
                    summaryTable,
                    "AVERAGE ORDER",
                    "PHP " + formatMoney(averageOrder),
                    summaryLabelFont,
                    summaryValueFont
            );

            document.add(summaryTable);

            Paragraph sectionTitle = new Paragraph(
                    "Orders Included in Report",
                    reportTitleFont
            );
            sectionTitle.setSpacingAfter(8);
            document.add(sectionTitle);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            table.setWidths(new float[]{
                    0.8f,
                    1.3f,
                    1.8f,
                    1.6f,
                    1.1f,
                    1.1f
            });

            addHeaderCell(table, "Order #", tableHeaderFont);
            addHeaderCell(table, "Date", tableHeaderFont);
            addHeaderCell(table, "Customer", tableHeaderFont);
            addHeaderCell(table, "Payment", tableHeaderFont);
            addHeaderCell(table, "Status", tableHeaderFont);
            addHeaderCell(table, "Total", tableHeaderFont);

            if (orders == null || orders.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(
                        new Phrase(
                                "No orders found for this report period.",
                                normalFont
                        )
                );

                emptyCell.setColspan(6);
                emptyCell.setHorizontalAlignment(
                        Element.ALIGN_CENTER
                );
                emptyCell.setPadding(12);
                emptyCell.setBorderColor(BORDER_COLOR);

                table.addCell(emptyCell);
            } else {
                boolean alternateRow = false;

                for (Order order : orders) {
                    Color rowColor = alternateRow
                            ? LIGHT_GRAY
                            : Color.WHITE;

                    addBodyCell(
                            table,
                            String.valueOf(order.getId()),
                            tableFont,
                            Element.ALIGN_CENTER,
                            rowColor
                    );

                    addBodyCell(
                            table,
                            order.getOrderDate() == null
                                    ? ""
                                    : order.getOrderDate()
                                    .format(
                                            DateTimeFormatter.ofPattern(
                                                    "MMM d, yyyy"
                                            )
                                    ),
                            tableFont,
                            Element.ALIGN_CENTER,
                            rowColor
                    );

                    addBodyCell(
                            table,
                            safeText(order.getCustomerName()),
                            tableFont,
                            Element.ALIGN_LEFT,
                            rowColor
                    );

                    addBodyCell(
                            table,
                            safeText(order.getPaymentMethod()),
                            tableFont,
                            Element.ALIGN_CENTER,
                            rowColor
                    );

                    addStatusCell(
                            table,
                            order.getStatus(),
                            rowColor
                    );

                    addBodyCell(
                            table,
                            order.getTotalAmount() == null
                                    ? "PHP 0.00"
                                    : "PHP "
                                    + formatMoney(
                                            order.getTotalAmount()
                                    ),
                            tableFont,
                            Element.ALIGN_RIGHT,
                            rowColor
                    );

                    alternateRow = !alternateRow;
                }
            }

            document.add(table);

        } catch (DocumentException exception) {
            throw new IllegalStateException(
                    "Failed to generate sales report PDF.",
                    exception
            );
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return outputStream.toByteArray();
    }

    private void addSummaryCell(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph labelParagraph =
                new Paragraph(label, labelFont);
        labelParagraph.setAlignment(Element.ALIGN_CENTER);

        Paragraph valueParagraph =
                new Paragraph(value, valueFont);
        valueParagraph.setAlignment(Element.ALIGN_CENTER);
        valueParagraph.setSpacingBefore(4);

        cell.addElement(labelParagraph);
        cell.addElement(valueParagraph);

        table.addCell(cell);
    }

    private void addHeaderCell(
            PdfPTable table,
            String value,
            Font font
    ) {

        PdfPCell cell = new PdfPCell(
                new Phrase(value, font)
        );

        cell.setBackgroundColor(DARK_HEADER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7);
        cell.setBorderColor(Color.WHITE);

        table.addCell(cell);
    }

    private void addBodyCell(
            PdfPTable table,
            String value,
            Font font,
            int alignment,
            Color backgroundColor
    ) {

        PdfPCell cell = new PdfPCell(
                new Phrase(value, font)
        );

        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(BORDER_COLOR);

        table.addCell(cell);
    }

    private void addStatusCell(
            PdfPTable table,
            String status,
            Color rowColor
    ) {

        String safeStatus = safeText(status);

        Color statusColor = switch (safeStatus.toLowerCase()) {
            case "delivered" -> new Color(25, 135, 84);
            case "processing" -> new Color(13, 110, 253);
            case "shipped" -> new Color(13, 202, 240);
            case "cancelled" -> new Color(220, 53, 69);
            default -> new Color(180, 125, 0);
        };

        Font statusFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                8,
                statusColor
        );

        addBodyCell(
                table,
                safeStatus,
                statusFont,
                Element.ALIGN_CENTER,
                rowColor
        );
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }

        BigDecimal formattedAmount = amount.setScale(
                2,
                RoundingMode.HALF_UP
        );

        return moneyFormat.format(formattedAmount);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private static class ReportPageEvent
            extends PdfPageEventHelper {

        private final Font footerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8,
                        Color.GRAY
                );

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document
        ) {

            String footerText =
                    "Generated by DABE TechStore E-Commerce System";

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase(footerText, footerFont),
                    document.left(),
                    20,
                    0
            );

            String pageText =
                    "Page " + writer.getPageNumber();

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(pageText, footerFont),
                    document.right(),
                    20,
                    0
            );
        }
    }
}