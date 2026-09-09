package com.lias.lias_backend.report.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.lias.lias_backend.equipment.entity.Equipment;
import com.lias.lias_backend.equipment.entity.EquipmentAssignment;
import com.lias.lias_backend.equipment.repository.EquipmentAssignmentRepository;
import com.lias.lias_backend.equipment.repository.EquipmentRepository;
import com.lias.lias_backend.event.entity.Event;
import com.lias.lias_backend.event.repository.EventRepository;
import com.lias.lias_backend.governance.entity.Mandate;
import com.lias.lias_backend.governance.repository.MandateRepository;
import com.lias.lias_backend.meeting.entity.Meeting;
import com.lias.lias_backend.meeting.repository.MeetingRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.AffiliationRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.publication.entity.Publication;
import com.lias.lias_backend.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberRepository memberRepository;
    private final PublicationRepository publicationRepository;
    private final EventRepository eventRepository;
    private final MeetingRepository meetingRepository;
    private final MandateRepository mandateRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentAssignmentRepository assignmentRepository;
    private final AffiliationRepository affiliationRepository;

    // ── FONTS ─────────────────────────────────────────────────
    private static PdfFont regular() throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA,
                PdfEncodings.WINANSI,
                PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
    }

    private static PdfFont bold() throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD,
                PdfEncodings.WINANSI,
                PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
    }

    private static PdfFont italic() throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE,
                PdfEncodings.WINANSI,
                PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
    }

    // ── COLORS (inline only — no static fields) ───────────────
    // Navy:      13,  43,  90
    // DarkGray:  80,  80,  80
    // MidGray:  160, 160, 160
    // LightGray:240, 240, 240
    // OffWhite: 250, 250, 250
    // Accent:    13,  43,  90  (same navy for minimal look)

    private String safe(String s) { return (s != null && !s.isBlank()) ? s : "-"; }

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINTS
    // ─────────────────────────────────────────────────────────

    public byte[] generateAnnualReport(int year) throws IOException {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year, 12, 31);
        return buildReport(
                "Annual Activity Report",
                String.valueOf(year),
                "January - December " + year,
                year, from, to);
    }

    public byte[] generateMonthlyReport(int year, int month) throws IOException {
        LocalDate from   = LocalDate.of(year, month, 1);
        LocalDate to     = from.withDayOfMonth(from.lengthOfMonth());
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return buildReport(
                "Monthly Activity Report",
                monthName + " " + year,
                monthName + " " + year,
                year, from, to);
    }

    // ─────────────────────────────────────────────────────────
    // CORE BUILDER
    // ─────────────────────────────────────────────────────────

    private byte[] buildReport(String title, String subtitle, String period,
                               int year, LocalDate from, LocalDate to) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf      = new PdfDocument(new PdfWriter(baos));
        Document    document = new Document(pdf, PageSize.A4);
        document.setMargins(60, 55, 60, 55);

        List<Member>              members     = memberRepository.findAll();
        List<Publication>         pubs        = publicationRepository.findByYear(year);
        List<Event>               events      = eventRepository.findEventsBetween(from, to);
        List<Meeting>             meetings    = meetingRepository.findMeetingsBetween(from, to);
        List<Mandate>             mandates    = mandateRepository.findActiveInPeriod(from, to);
        List<Equipment>           equipment   = equipmentRepository.findByArrivalDateBetween(from, to);
        List<EquipmentAssignment> assignments = assignmentRepository.findByAssignmentDateBetween(from, to);

        addCoverPage(document, title, subtitle, period, members, pubs, events, meetings);
        addGovernanceSection(document, mandates);
        addMembersSection(document, members);
        addPublicationsSection(document, pubs);
        addEventsSection(document, events);
        addMeetingsSection(document, meetings);
        addEquipmentSection(document, equipment, assignments);

        document.close();

        // Second pass — stamp header/footer on every page
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PdfDocument pdfForHeaders = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(baos.toByteArray())),
                new PdfWriter(baos2));

        int totalPages = pdfForHeaders.getNumberOfPages();
        for (int i = 1; i <= totalPages; i++) {
            addPageFooter(pdfForHeaders, i, totalPages);
            if (i > 1) addPageHeader(pdfForHeaders, title + " " + subtitle, i);
        }
        pdfForHeaders.close();

        return baos2.toByteArray();
    }

    // ─────────────────────────────────────────────────────────
    // COVER PAGE — clean, minimal, elegant
    // ─────────────────────────────────────────────────────────

    private void addCoverPage(Document doc, String title, String subtitle, String period,
                              List<Member> members, List<Publication> pubs,
                              List<Event> events, List<Meeting> meetings) throws IOException {

        // Top thin navy rule
        doc.add(new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .addCell(new Cell()
                        .setHeight(4)
                        .setBackgroundColor(new DeviceRgb(13, 43, 90))
                        .setBorder(Border.NO_BORDER)));

        doc.add(new Paragraph("\n\n\n"));

        // Institution name — small, spaced, gray
        doc.add(new Paragraph("FACULTE DES SCIENCES BEN M'SIK  —  CASABLANCA")
                .setFont(regular()).setFontSize(8)
                .setFontColor(new DeviceRgb(160, 160, 160))
                .setTextAlignment(TextAlignment.LEFT)
                .setCharacterSpacing(1.5f));

        doc.add(new Paragraph("\n"));

        // Big lab name
        doc.add(new Paragraph("LIAS Laboratory")
                .setFont(bold()).setFontSize(30)
                .setFontColor(new DeviceRgb(13, 43, 90))
                .setTextAlignment(TextAlignment.LEFT));

        // Subtitle in italic gray
        doc.add(new Paragraph("Laboratoire d'Informatique et Applications des Sciences")
                .setFont(italic()).setFontSize(11)
                .setFontColor(new DeviceRgb(100, 100, 100))
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(2));

        doc.add(new Paragraph("\n\n"));

        // Thin divider line
        doc.add(new LineSeparator(new SolidLine(0.5f))
                .setStrokeColor(new DeviceRgb(200, 200, 200))
                .setWidth(UnitValue.createPercentValue(100)));

        doc.add(new Paragraph("\n\n"));

        // Report title — large and clean
        doc.add(new Paragraph(title)
                .setFont(regular()).setFontSize(22)
                .setFontColor(new DeviceRgb(30, 30, 30))
                .setTextAlignment(TextAlignment.LEFT));

        // Year / period — navy accent
        doc.add(new Paragraph(subtitle)
                .setFont(bold()).setFontSize(22)
                .setFontColor(new DeviceRgb(13, 43, 90))
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(-4));

        doc.add(new Paragraph("\n\n\n"));

        // Stats row — 4 simple boxes
        Table stats = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        addStatBox(stats, String.valueOf(members.size()), "Members");
        addStatBox(stats, String.valueOf(pubs.size()), "Publications");
        addStatBox(stats, String.valueOf(events.size()), "Events");
        addStatBox(stats, String.valueOf(meetings.size()), "Meetings");
        doc.add(stats);

        doc.add(new Paragraph("\n\n\n\n\n"));

        // Bottom meta info
        doc.add(new LineSeparator(new SolidLine(0.5f))
                .setStrokeColor(new DeviceRgb(200, 200, 200))
                .setWidth(UnitValue.createPercentValue(100)));

        doc.add(new Paragraph("Generated on " + LocalDate.now()
                + "   |   Period: " + period)
                .setFont(regular()).setFontSize(8)
                .setFontColor(new DeviceRgb(160, 160, 160))
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginTop(6));

        doc.add(new AreaBreak());
    }

    private void addStatBox(Table table, String value, String label) throws IOException {
        table.addCell(new Cell()
                .add(new Paragraph(value)
                        .setFont(bold()).setFontSize(28)
                        .setFontColor(new DeviceRgb(13, 43, 90))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(2))
                .add(new Paragraph(label)
                        .setFont(regular()).setFontSize(9)
                        .setFontColor(new DeviceRgb(120, 120, 120))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(new DeviceRgb(13, 43, 90), 2))
                .setPaddingTop(10).setPaddingBottom(10).setMargin(4));
    }

    // ─────────────────────────────────────────────────────────
    // SECTION HELPERS
    // ─────────────────────────────────────────────────────────

    private void addSectionTitle(Document doc, String number, String title) throws IOException {
        doc.add(new Paragraph("\n"));

        // Section number — tiny navy
        doc.add(new Paragraph(number)
                .setFont(bold()).setFontSize(8)
                .setFontColor(new DeviceRgb(13, 43, 90))
                .setCharacterSpacing(2f)
                .setMarginBottom(2));

        // Section title — large dark
        doc.add(new Paragraph(title)
                .setFont(bold()).setFontSize(16)
                .setFontColor(new DeviceRgb(20, 20, 20))
                .setMarginBottom(4)
                .setMarginTop(0));

        // Thin underline
        doc.add(new LineSeparator(new SolidLine(0.5f))
                .setStrokeColor(new DeviceRgb(200, 200, 200))
                .setWidth(UnitValue.createPercentValue(100)));

        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addEmptyNote(Document doc, String text) throws IOException {
        doc.add(new Paragraph(text)
                .setFont(italic()).setFontSize(9)
                .setFontColor(new DeviceRgb(160, 160, 160))
                .setMarginTop(4).setMarginLeft(4));
    }

    private Table createTable(float... widths) {
        return new Table(UnitValue.createPercentArray(widths))
                .setWidth(UnitValue.createPercentValue(100));
    }

    private Cell th(String text) throws IOException {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(bold()).setFontSize(8)
                        .setFontColor(new DeviceRgb(80, 80, 80))
                        .setCharacterSpacing(0.5f))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(13, 43, 90), 1.5f))
                .setPaddingBottom(5).setPaddingTop(4)
                .setBackgroundColor(ColorConstants.WHITE);
    }

    private Cell td(String text, boolean shaded) throws IOException {
        return new Cell()
                .add(new Paragraph(text != null && !text.isBlank() ? text : "-")
                        .setFont(regular()).setFontSize(9)
                        .setFontColor(new DeviceRgb(50, 50, 50)))
                .setBackgroundColor(shaded
                        ? new DeviceRgb(248, 248, 248) : ColorConstants.WHITE)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(235, 235, 235), 0.5f))
                .setPadding(6);
    }

    // ─────────────────────────────────────────────────────────
    // SECTION 1 — GOVERNANCE
    // ─────────────────────────────────────────────────────────

    private void addGovernanceSection(Document doc, List<Mandate> mandates) throws IOException {
        addSectionTitle(doc, "01", "Governance & Mandates");

        if (mandates.isEmpty()) {
            addEmptyNote(doc, "No mandates recorded for this period.");
            return;
        }

        Table table = createTable(28, 22, 20, 15, 15);
        table.addCell(th("MEMBER"));
        table.addCell(th("ROLE"));
        table.addCell(th("TEAM"));
        table.addCell(th("START DATE"));
        table.addCell(th("END DATE"));

        for (int i = 0; i < mandates.size(); i++) {
            Mandate m = mandates.get(i);
            boolean s = i % 2 == 1;
            String name = (m.getMember() != null
                    && m.getMember().getFirstName() != null)
                    ? m.getMember().getFirstName() + " " + m.getMember().getLastName()
                    : "-";
            table.addCell(td(name, s));
            table.addCell(td(m.getRole() != null ? m.getRole().name() : "-", s));
            table.addCell(td(safe(m.getTeam()), s));
            table.addCell(td(m.getStartDate() != null ? m.getStartDate().toString() : "-", s));
            table.addCell(td(m.getEndDate() != null ? m.getEndDate().toString() : "Active", s));
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────────
    // SECTION 2 — MEMBERS
    // ─────────────────────────────────────────────────────────

    private void addMembersSection(Document doc, List<Member> members) throws IOException {
        addSectionTitle(doc, "02", "Laboratory Members");

        // Status summary — clean inline text
        Map<String, Long> byStatus = members.stream().collect(Collectors.groupingBy(
                m -> m.getStatus() != null ? m.getStatus().name() : "UNKNOWN",
                Collectors.counting()));

        StringBuilder summary = new StringBuilder();
        byStatus.forEach((status, count) ->
                summary.append(count).append(" ").append(status).append("   "));

        doc.add(new Paragraph(summary.toString().trim())
                .setFont(regular()).setFontSize(9)
                .setFontColor(new DeviceRgb(100, 100, 100))
                .setMarginBottom(8));

        Table table = createTable(28, 18, 22, 18, 14);
        table.addCell(th("FULL NAME"));
        table.addCell(th("STATUS"));
        table.addCell(th("ESTABLISHMENT"));
        table.addCell(th("TEAM"));
        table.addCell(th("HIRE DATE"));

        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            boolean s = i % 2 == 1;
            String name = (m.getFirstName() != null)
                    ? m.getFirstName() + " " + m.getLastName() : "-";
            String team = affiliationRepository.findActiveByMemberId(m.getId())
                    .map(a -> safe(a.getTeam())).orElse("-");
            table.addCell(td(name, s));
            table.addCell(td(m.getStatus() != null ? m.getStatus().name() : "-", s));
            table.addCell(td(safe(m.getEstablishment()), s));
            table.addCell(td(team, s));
            table.addCell(td(m.getHireDate() != null ? m.getHireDate().toString() : "-", s));
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────────
    // SECTION 3 — PUBLICATIONS
    // ─────────────────────────────────────────────────────────

    private void addPublicationsSection(Document doc, List<Publication> publications) throws IOException {
        addSectionTitle(doc, "03", "Scientific Publications");

        if (publications.isEmpty()) {
            addEmptyNote(doc, "No publications recorded for this period.");
            return;
        }

        // Type summary
        Map<String, Long> byType = publications.stream().collect(Collectors.groupingBy(
                p -> p.getType() != null ? p.getType().name() : "OTHER",
                Collectors.counting()));

        StringBuilder summary = new StringBuilder();
        byType.forEach((type, count) ->
                summary.append(count).append(" ").append(type).append("   "));

        doc.add(new Paragraph(summary.toString().trim())
                .setFont(regular()).setFontSize(9)
                .setFontColor(new DeviceRgb(100, 100, 100))
                .setMarginBottom(8));

        Table table = createTable(36, 22, 14, 14, 14);
        table.addCell(th("TITLE"));
        table.addCell(th("AUTHORS"));
        table.addCell(th("TYPE"));
        table.addCell(th("VENUE"));
        table.addCell(th("TEAM"));

        for (int i = 0; i < publications.size(); i++) {
            Publication p = publications.get(i);
            boolean s = i % 2 == 1;
            String venue = p.getJournal() != null ? p.getJournal()
                    : p.getConference() != null ? p.getConference() : "-";
            table.addCell(td(safe(p.getTitle()), s));
            table.addCell(td(safe(p.getAuthors()), s));
            table.addCell(td(p.getType() != null ? p.getType().name() : "-", s));
            table.addCell(td(venue, s));
            table.addCell(td(safe(p.getTeam()), s));
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────────
    // SECTION 4 — EVENTS
    // ─────────────────────────────────────────────────────────

    private void addEventsSection(Document doc, List<Event> events) throws IOException {
        addSectionTitle(doc, "04", "Scientific Events");

        if (events.isEmpty()) {
            addEmptyNote(doc, "No events recorded for this period.");
            return;
        }

        Table table = createTable(32, 14, 22, 14, 18);
        table.addCell(th("TITLE"));
        table.addCell(th("TYPE"));
        table.addCell(th("LOCATION"));
        table.addCell(th("DATE"));
        table.addCell(th("STATUS"));

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            boolean s = i % 2 == 1;
            table.addCell(td(safe(e.getTitle()), s));
            table.addCell(td(e.getType() != null ? e.getType().name() : "-", s));
            table.addCell(td(safe(e.getLocation()), s));
            table.addCell(td(e.getStartDate() != null ? e.getStartDate().toString() : "-", s));
            table.addCell(td(e.getStatus() != null ? e.getStatus().name() : "-", s));
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────────
    // SECTION 5 — MEETINGS
    // ─────────────────────────────────────────────────────────

    private void addMeetingsSection(Document doc, List<Meeting> meetings) throws IOException {
        addSectionTitle(doc, "05", "Meetings & Proces-Verbaux");

        if (meetings.isEmpty()) {
            addEmptyNote(doc, "No meetings recorded for this period.");
            return;
        }

        long withPV = meetings.stream().filter(m -> m.getPvFilePath() != null).count();
        doc.add(new Paragraph(meetings.size() + " meetings total   |   "
                + withPV + " with PV   |   "
                + (meetings.size() - withPV) + " without PV")
                .setFont(regular()).setFontSize(9)
                .setFontColor(new DeviceRgb(100, 100, 100))
                .setMarginBottom(8));

        Table table = createTable(38, 18, 20, 24);
        table.addCell(th("TITLE"));
        table.addCell(th("DATE"));
        table.addCell(th("STATUS"));
        table.addCell(th("PV STATUS"));

        for (int i = 0; i < meetings.size(); i++) {
            Meeting m = meetings.get(i);
            boolean s = i % 2 == 1;
            table.addCell(td(safe(m.getTitle()), s));
            table.addCell(td(m.getDate() != null ? m.getDate().toString() : "-", s));
            table.addCell(td(m.getStatus() != null ? m.getStatus().name() : "-", s));
            table.addCell(td(m.getPvFilePath() != null ? "Uploaded" : "Pending", s));
        }
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────────
    // SECTION 6 — EQUIPMENT
    // ─────────────────────────────────────────────────────────

    private void addEquipmentSection(Document doc, List<Equipment> arrivals,
                                     List<EquipmentAssignment> assignments) throws IOException {
        addSectionTitle(doc, "06", "Equipment & Distribution");

        doc.add(new Paragraph(arrivals.size() + " arrivals   |   "
                + assignments.size() + " distributions")
                .setFont(regular()).setFontSize(9)
                .setFontColor(new DeviceRgb(100, 100, 100))
                .setMarginBottom(8));

        if (!arrivals.isEmpty()) {
            doc.add(new Paragraph("Arrivals")
                    .setFont(bold()).setFontSize(10)
                    .setFontColor(new DeviceRgb(13, 43, 90))
                    .setMarginBottom(4));

            Table table = createTable(28, 22, 12, 20, 18);
            table.addCell(th("NAME"));
            table.addCell(th("SERIAL NUMBER"));
            table.addCell(th("QTY"));
            table.addCell(th("CONDITION"));
            table.addCell(th("ARRIVAL DATE"));

            for (int i = 0; i < arrivals.size(); i++) {
                Equipment e = arrivals.get(i);
                boolean s = i % 2 == 1;
                table.addCell(td(safe(e.getName()), s));
                table.addCell(td(safe(e.getSerialNumber()), s));
                table.addCell(td(String.valueOf(e.getQuantity()), s));
                table.addCell(td(e.getCondition() != null ? e.getCondition().name() : "-", s));
                table.addCell(td(e.getArrivalDate() != null ? e.getArrivalDate().toString() : "-", s));
            }
            doc.add(table);
            doc.add(new Paragraph("\n").setFontSize(6));
        }

        if (!assignments.isEmpty()) {
            doc.add(new Paragraph("Distribution Records")
                    .setFont(bold()).setFontSize(10)
                    .setFontColor(new DeviceRgb(13, 43, 90))
                    .setMarginBottom(4));

            Table table = createTable(28, 28, 22, 22);
            table.addCell(th("EQUIPMENT"));
            table.addCell(th("ASSIGNED TO"));
            table.addCell(th("DATE"));
            table.addCell(th("RETURNED"));

            for (int i = 0; i < assignments.size(); i++) {
                EquipmentAssignment a = assignments.get(i);
                boolean s = i % 2 == 1;
                String eq = a.getEquipment() != null ? a.getEquipment().getName() : "-";
                String mn = (a.getMember() != null && a.getMember().getFirstName() != null)
                        ? a.getMember().getFirstName() + " " + a.getMember().getLastName() : "-";
                table.addCell(td(eq, s));
                table.addCell(td(mn, s));
                table.addCell(td(a.getAssignmentDate() != null ? a.getAssignmentDate().toString() : "-", s));
                table.addCell(td(a.getReturnDate() != null ? a.getReturnDate().toString() : "Still assigned", s));
            }
            doc.add(table);
        }
    }

    // ─────────────────────────────────────────────────────────
    // HEADER / FOOTER
    // ─────────────────────────────────────────────────────────

    private void addPageHeader(PdfDocument pdf, String reportTitle, int pageNum) throws IOException {
        PdfFont f   = regular();
        float width = pdf.getPage(pageNum).getPageSize().getWidth();
        float top   = pdf.getPage(pageNum).getPageSize().getTop();
        Canvas canvas = new Canvas(pdf.getPage(pageNum), pdf.getPage(pageNum).getPageSize());
        canvas.showTextAligned(
                new Paragraph("LIAS Laboratory")
                        .setFont(bold()).setFontSize(8)
                        .setFontColor(new DeviceRgb(13, 43, 90)),
                55, top - 28, TextAlignment.LEFT);
        canvas.showTextAligned(
                new Paragraph(reportTitle)
                        .setFont(f).setFontSize(8)
                        .setFontColor(new DeviceRgb(160, 160, 160)),
                width / 2, top - 28, TextAlignment.CENTER);
        canvas.close();
    }

    private void addPageFooter(PdfDocument pdf, int pageNum, int totalPages) throws IOException {
        PdfFont f    = regular();
        float width  = pdf.getPage(pageNum).getPageSize().getWidth();
        float bottom = pdf.getPage(pageNum).getPageSize().getBottom();
        Canvas canvas = new Canvas(pdf.getPage(pageNum), pdf.getPage(pageNum).getPageSize());
        canvas.showTextAligned(
                new Paragraph("Faculte des Sciences Ben M'Sik - Casablanca")
                        .setFont(f).setFontSize(7.5f)
                        .setFontColor(new DeviceRgb(180, 180, 180)),
                55, bottom + 22, TextAlignment.LEFT);
        canvas.showTextAligned(
                new Paragraph(pageNum + " / " + totalPages)
                        .setFont(f).setFontSize(7.5f)
                        .setFontColor(new DeviceRgb(130, 130, 130)),
                width - 55, bottom + 22, TextAlignment.RIGHT);
        canvas.close();
    }
}