# Student Upload Feature for Groups

## Overview
This feature allows you to upload Excel files containing student data to add "required attendees" to specific groups. The students are linked to groups within specific courses through the `STUDENT_IN_GROUP` table.

## Excel File Format
The Excel file should have the following structure:

| Column A  | Column B   | Column C       |
|-----------|------------|----------------|
| Last Name | First Name | Student Number |

### Example:
```
Doe         John        r1234567
Smith       Jane        r2345678
Johnson     Bob         r3456789
```

### Student Number Format
- Must start with 'r' followed by exactly 7 digits
- Examples: `r1234567`, `r9876543`
- Invalid: `1234567`, `r123456`, `r12345678`

## API Endpoint

### Upload Student Attendees
```
POST /groups/upload-attendees
```

#### Parameters:
- `file` (multipart file): The Excel file to upload (.xlsx or .xls)
- `groupId` (Long): ID of the group to add students to
- `courseId` (Long): ID of the course (for validation)

#### Example Request:
```bash
curl -X POST "http://localhost:8080/groups/upload-attendees" \
  -F "file=@students.xlsx" \
  -F "groupId=1" \
  -F "courseId=1"
```

#### Example Success Response:
```json
{
    "message": "Required attendees uploaded successfully",
    "count": 3,
    "students": [
        {
            "id": 1,
            "name": "John",
            "lastName": "Doe",
            "studentNumber": "r1234567",
            "assignedGroups": [...]
        },
        {
            "id": 2,
            "name": "Jane",
            "lastName": "Smith",
            "studentNumber": "r2345678",
            "assignedGroups": [...]
        }
    ]
}
```

#### Example Error Response:
```json
{
    "error": "Failed to process file: Group not found with ID: 999"
}
```

## Business Logic

### Student Processing:
1. **Existing Students**: If a student with the same student number already exists in the database, they will be added to the group (if not already a member)
2. **New Students**: If a student doesn't exist, a new student record is created and added to the group
3. **Duplicate Prevention**: Students already in the group won't be added again

### Validation:
- Group must exist and belong to the specified course
- Course must exist
- Excel file must be valid (.xlsx or .xls format)
- Student numbers must follow the required format (r + 7 digits)
- All required fields (lastName, firstName, studentNumber) must be present

### Database Tables Affected:
- `STUDENTS`: New students are inserted here
- `STUDENT_IN_GROUP`: Relationships between students and groups are created here
- `GROUPTABLE`: Updated with new student relationships

## Frontend Integration

### HTML Form Example:
You can use the provided `/upload-students.html` page, or integrate into your existing frontend:

```html
<form id="uploadForm" enctype="multipart/form-data">
    <input type="file" name="file" accept=".xlsx,.xls" required>
    <input type="number" name="groupId" placeholder="Group ID" required>
    <input type="number" name="courseId" placeholder="Course ID" required>
    <button type="submit">Upload Students</button>
</form>
```

### JavaScript Example:
```javascript
const uploadStudents = async (file, groupId, courseId) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('groupId', groupId);
    formData.append('courseId', courseId);

    try {
        const response = await fetch('/groups/upload-attendees', {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error('Upload failed');
        }

        const result = await response.json();
        console.log(`Successfully uploaded ${result.count} students`);
        return result;
    } catch (error) {
        console.error('Error uploading students:', error);
        throw error;
    }
};
```

## Error Handling

### Common Errors:
1. **File Format**: "Only Excel files (.xlsx, .xls) are allowed"
2. **Empty File**: "File is empty"
3. **Invalid Group**: "Group not found with ID: X"
4. **Invalid Course**: "Course not found with ID: X"
5. **Group-Course Mismatch**: "Group does not belong to the specified course"
6. **Invalid Student Number**: "Invalid student number format at row X: rXXXXXX"
7. **Missing Data**: "Incomplete data at row X: lastName='...', firstName='...', studentNumber='...'"

## Testing

### Manual Testing:
1. Navigate to `http://localhost:8080/upload-students.html`
2. Prepare an Excel file with the correct format
3. Enter valid Group ID and Course ID
4. Upload the file and verify the response

### Sample Test Data:
Create an Excel file with:
```
Doe         John        r1111111
Smith       Jane        r2222222
Brown       Alice       r3333333
```

## Integration with Existing Features

This feature integrates with:
- **Registration System**: The uploaded students are the "required attendees" that can be compared against actual registrations (check-ins/check-outs)
- **Group Management**: Students are automatically linked to groups through the existing many-to-many relationship
- **Course System**: Validation ensures students are only added to groups within the correct course context

## Future Enhancements

Potential improvements could include:
1. Bulk student removal from groups
2. CSV file support in addition to Excel
3. Student photo uploads
4. Attendance reporting comparing required vs actual attendees
5. Group-specific student role assignments