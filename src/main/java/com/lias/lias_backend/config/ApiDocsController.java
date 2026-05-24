package com.lias.lias_backend.config;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiDocsController {

    @GetMapping(value = "/api-docs", produces = MediaType.TEXT_HTML_VALUE)
    public String getApiDocs() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LIAS API Documentation</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f8f9fa; color: #2d3748; }
        .header { background: #0d2b5a; color: white; padding: 40px 60px; }
        .header h1 { font-size: 28px; font-weight: 600; margin-bottom: 6px; }
        .header p { font-size: 14px; opacity: 0.7; }
        .header .version { display: inline-block; background: rgba(255,255,255,0.15); padding: 3px 10px; border-radius: 12px; font-size: 12px; margin-top: 10px; }
        .auth-note { background: #fff8e1; border-left: 4px solid #f6a623; padding: 14px 24px; margin: 24px 60px; border-radius: 4px; font-size: 13px; color: #7a5c00; }
        .container { padding: 10px 60px 60px; }
        .module { background: white; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); overflow: hidden; }
        .module-title { background: #0d2b5a; color: white; padding: 12px 20px; font-size: 13px; font-weight: 600; letter-spacing: 0.5px; text-transform: uppercase; }
        .endpoint { display: flex; align-items: flex-start; padding: 12px 20px; border-bottom: 1px solid #f0f0f0; gap: 16px; }
        .endpoint:last-child { border-bottom: none; }
        .endpoint:hover { background: #f8f9fa; }
        .method { font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 4px; min-width: 60px; text-align: center; letter-spacing: 0.5px; flex-shrink: 0; margin-top: 2px; }
        .GET    { background: #e8f5e9; color: #2e7d32; }
        .POST   { background: #e3f2fd; color: #1565c0; }
        .PUT    { background: #fff3e0; color: #e65100; }
        .PATCH  { background: #f3e5f5; color: #6a1b9a; }
        .DELETE { background: #ffebee; color: #c62828; }
        .path { font-family: 'Courier New', monospace; font-size: 13px; color: #0d2b5a; font-weight: 500; min-width: 300px; flex-shrink: 0; }
        .desc { font-size: 13px; color: #718096; line-height: 1.5; }
        .tag { display: inline-block; background: #edf2f7; color: #4a5568; font-size: 11px; padding: 2px 7px; border-radius: 10px; margin-left: 6px; }
        .tag.admin { background: #fed7d7; color: #c53030; }
        .tag.director { background: #bee3f8; color: #2b6cb0; }
    </style>
</head>
<body>

<div class="header">
    <h1>LIAS Laboratory — API Documentation</h1>
    <p>Laboratoire d'Informatique et Applications des Sciences</p>
    <div class="version">v1.0.0 &nbsp;|&nbsp; Base URL: http://localhost:8080</div>
</div>

<div class="auth-note">
    All endpoints except <code>/api/auth/**</code> and <code>/api-docs</code> require:
    <strong>Authorization: Bearer &lt;token&gt;</strong>
</div>

<div class="container">

    <div class="module">
        <div class="module-title">Authentication</div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/auth/register</span><span class="desc">Register new member. Body: {email, password}</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/auth/login</span><span class="desc">Login and get JWT token. Body: {email, password}</span></div>
    </div>

    <div class="module">
        <div class="module-title">Members</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/members/me</span><span class="desc">Get my profile</span></div>
        <div class="endpoint"><span class="method PUT">PUT</span><span class="path">/api/members/me</span><span class="desc">Update my profile. Body: {firstName, lastName, biography, interests, establishment, originLaboratory}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/members/{id}</span><span class="desc">Get member by ID</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/members/me/photo</span><span class="desc">Upload profile photo. Body: multipart/form-data, field: photo</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/members/{id}/photo</span><span class="desc">Get member photo. Returns image file</span></div>
    </div>

    <div class="module">
        <div class="module-title">Admin <span class="tag admin">ADMIN only</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/admin/members</span><span class="desc">List all members</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/admin/members/pending</span><span class="desc">List pending members awaiting approval</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/admin/members/{userId}/approve</span><span class="desc">Approve a pending member</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/admin/members/{userId}/reject</span><span class="desc">Reject a pending member</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/admin/members/{userId}/freeze</span><span class="desc">Freeze an active member</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/admin/members/{userId}/activate</span><span class="desc">Reactivate a frozen or disabled member</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/admin/members/{userId}/role</span><span class="desc">Change member role. Body: {role}</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/admin/members/{userId}/status</span><span class="desc">Change member status. Body: {status}</span></div>
    </div>

    <div class="module">
        <div class="module-title">Affiliations</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/affiliations</span><span class="desc">Get my affiliations history</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/affiliations</span><span class="desc">Add affiliation. Body: {laboratory, team, startDate}</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/affiliations/{id}</span><span class="desc">Update affiliation. Body: {team, endDate}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/affiliations/member/{memberId}</span><span class="desc">Get all affiliations for a specific member</span></div>
    </div>

    <div class="module">
        <div class="module-title">Publications</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/publications</span><span class="desc">List all publications. Params: ?year={year}&amp;team={team}&amp;author={author}</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/publications</span><span class="desc">Create publication. Body: {title, authors, journal, conference, doi, url, abstractText, team, year, type}</span></div>
        <div class="endpoint"><span class="method PUT">PUT</span><span class="path">/api/publications/{id}</span><span class="desc">Update publication. Body: same as create</span></div>
        <div class="endpoint"><span class="method DELETE">DELETE</span><span class="path">/api/publications/{id}</span><span class="desc">Delete publication</span></div>
    </div>

    <div class="module">
        <div class="module-title">Events</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/events</span><span class="desc">List all events. Params: ?type={type}&amp;status={status}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/events/{id}</span><span class="desc">Get event by ID</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/events</span><span class="desc">Create event. Body: {title, description, location, startDate, endDate, type, status, edition, website}</span></div>
        <div class="endpoint"><span class="method PUT">PUT</span><span class="path">/api/events/{id}</span><span class="desc">Update event. Body: same as create</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/events/{id}/status</span><span class="desc">Update event status. Body: {status}</span></div>
        <div class="endpoint"><span class="method DELETE">DELETE</span><span class="path">/api/events/{id}</span><span class="desc">Delete event</span></div>
    </div>

    <div class="module">
        <div class="module-title">Documents</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/documents</span><span class="desc">List all documents. Params: ?type={type}&amp;eventId={eventId}</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/documents/upload</span><span class="desc">Upload document. Body: multipart/form-data, fields: file, description, type, eventId</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/documents/{id}/download</span><span class="desc">Download document. Returns file</span></div>
        <div class="endpoint"><span class="method DELETE">DELETE</span><span class="path">/api/documents/{id}</span><span class="desc">Delete document</span></div>
    </div>

    <div class="module">
        <div class="module-title">Meetings & Proces-Verbaux</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/meetings</span><span class="desc">List all meetings. Params: ?status={status}</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/meetings</span><span class="desc">Create meeting. Body: {title, description, location, agenda, date, status}</span></div>
        <div class="endpoint"><span class="method PUT">PUT</span><span class="path">/api/meetings/{id}</span><span class="desc">Update meeting. Body: same as create</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/meetings/{id}/status</span><span class="desc">Update meeting status. Body: {status}</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/meetings/{id}/pv</span><span class="desc">Upload PV file. Body: multipart/form-data, field: file</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/meetings/{id}/pv/download</span><span class="desc">Download PV file. Returns file</span></div>
        <div class="endpoint"><span class="method DELETE">DELETE</span><span class="path">/api/meetings/{id}</span><span class="desc">Delete meeting</span></div>
    </div>

    <div class="module">
        <div class="module-title">Governance & Mandates</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/mandates</span><span class="desc">List all mandates</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/mandates</span><span class="desc">Create mandate. Body: {memberId, role, team, startDate, endDate}</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/mandates/{id}</span><span class="desc">Update mandate. Body: {role, team, endDate}</span></div>
        <div class="endpoint"><span class="method DELETE">DELETE</span><span class="path">/api/mandates/{id}</span><span class="desc">Delete mandate</span></div>
    </div>

    <div class="module">
        <div class="module-title">Equipment & Distribution</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment</span><span class="desc">List all equipment. Params: ?search={keyword}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/available</span><span class="desc">List equipment with available quantity</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/{id}</span><span class="desc">Get equipment by ID</span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/equipment</span><span class="desc">Register equipment arrival. <span class="tag admin">ADMIN</span> Body: {name, serialNumber, quantity, arrivalDate, condition, description, notes}</span></div>
        <div class="endpoint"><span class="method PUT">PUT</span><span class="path">/api/equipment/{id}</span><span class="desc">Update equipment. <span class="tag admin">ADMIN</span></span></div>
        <div class="endpoint"><span class="method DELETE">DELETE</span><span class="path">/api/equipment/{id}</span><span class="desc">Delete equipment. <span class="tag admin">ADMIN</span></span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/equipment/assignments</span><span class="desc">Assign equipment to member. <span class="tag admin">ADMIN</span> Body: {memberId, equipmentId, quantity, assignmentNote, fromRequestId}</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/equipment/assignments/{id}/return</span><span class="desc">Return equipment. <span class="tag admin">ADMIN</span> Body: {returnNote}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/assignments</span><span class="desc">All active assignments. <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span></span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/assignments/member/{memberId}</span><span class="desc">Assignment history for a member. <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span></span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/{id}/assignments</span><span class="desc">Assignment history for equipment item. <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span></span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/assignments/no-equipment</span><span class="desc">Members who never received equipment. <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span></span></div>
        <div class="endpoint"><span class="method POST">POST</span><span class="path">/api/equipment/requests</span><span class="desc">Submit equipment request. Body: {equipmentName, equipmentDescription, quantityRequested, justification}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/requests/my</span><span class="desc">My equipment requests</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/equipment/requests</span><span class="desc">All equipment requests. <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span> Params: ?status={status}</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/equipment/requests/{id}/validate</span><span class="desc">Validate equipment request. <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span> Body: {decision, validationNote, equipmentId}</span></div>
    </div>

    <div class="module">
        <div class="module-title">Notifications</div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/notifications</span><span class="desc">Get my notifications</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/notifications/unread</span><span class="desc">Get my unread notifications</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/notifications/unread/count</span><span class="desc">Get unread notifications count</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/notifications/{id}/read</span><span class="desc">Mark notification as read</span></div>
        <div class="endpoint"><span class="method PATCH">PATCH</span><span class="path">/api/notifications/read-all</span><span class="desc">Mark all notifications as read</span></div>
    </div>

    <div class="module">
        <div class="module-title">Reports <span class="tag admin">ADMIN</span> <span class="tag director">DIRECTOR</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/report/annual</span><span class="desc">Generate and download annual PDF report. Params: ?year={year}</span></div>
        <div class="endpoint"><span class="method GET">GET</span><span class="path">/api/report/monthly</span><span class="desc">Generate and download monthly PDF report. Params: ?year={year}&amp;month={month}</span></div>
    </div>

</div>
</body>
</html>
""";
    }
}