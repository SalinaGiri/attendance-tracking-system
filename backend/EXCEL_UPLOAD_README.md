# Excel Upload Feature for Registrations

## Overview
This feature allows you to upload Excel files containing registration data. Each row in the Excel file will be processed and saved as a Registration object in the database.

## Excel File Format
The Excel file should have the following structure:

| Column A | Column B | Column C |
|----------|----------|----------|
| Email    | Date     | Time     |

### Example:
```
john@example.com    2024-01-15    09:30:00
jane@example.com    2024-01-15    14:45:00
bob@example.com     2024-01-16    08:15:00
```

## Supported Date/Time Formats
The service supports multiple date and time formats:

### Date formats:
- `yyyy-MM-dd` (e.g., 2024-01-15)
- `dd/MM/yyyy` (e.g., 15/01/2024)
- `MM/dd/yyyy` (e.g., 01/15/2024)

### Time formats:
- `HH:mm:ss` (e.g., 09:30:00)
- `HH:mm` (e.g., 09:30)

### Combined date-time formats:
- `yyyy-MM-dd HH:mm:ss`
- `dd/MM/yyyy HH:mm:ss`
- `MM/dd/yyyy HH:mm:ss`
- `yyyy-MM-dd HH:mm`
- `dd/MM/yyyy HH:mm`
- `MM/dd/yyyy HH:mm`

## API Endpoint

### Upload Excel File
```
POST /registrations/upload
```

#### Parameters:
- `file` (multipart file): The Excel file to upload (.xlsx or .xls)
- `type` (string): Registration type (Checkin, Checkout, LunchCheckin, LunchCheckout)
- `eventId` (long, optional): ID of the associated event

#### Example Response:
```json
{
    "message": "Registrations uploaded successfully",
    "count": 3,
    "registrations": [
        {
            "id": 1,
            "date": "2024-01-15T09:30:00",
            "type": "Checkin",
            "user": {
                "id": 1,
                "email": "john@example.com",
                "name": "John Doe"
            }
        }
    ]
}
```

#### Error Response:
```json
{
    "error": "Failed to process file: User not found for email: invalid@example.com"
}
```

## Frontend Integration

### TypeScript/JavaScript Example:
```typescript
const uploadRegistrations = async (file: File, type: string, eventId?: number) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    if (eventId) {
        formData.append('eventId', eventId.toString());
    }

    try {
        const response = await fetch('http://localhost:8080/registrations/upload', {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error('Upload failed');
        }

        const result = await response.json();
        console.log('Upload successful:', result);
        return result;
    } catch (error) {
        console.error('Upload error:', error);
        throw error;
    }
};
```

### React Component Example:
```typescript
import React, { useState } from 'react';

const RegistrationUpload: React.FC = () => {
    const [file, setFile] = useState<File | null>(null);
    const [type, setType] = useState<string>('Checkin');
    const [loading, setLoading] = useState<boolean>(false);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files && event.target.files[0]) {
            setFile(event.target.files[0]);
        }
    };

    const handleUpload = async () => {
        if (!file) return;

        setLoading(true);
        try {
            const result = await uploadRegistrations(file, type);
            alert(`Successfully uploaded ${result.count} registrations`);
        } catch (error) {
            alert('Upload failed: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h2>Upload Registration Data</h2>
            <div>
                <label>
                    Excel File:
                    <input type="file" accept=".xlsx,.xls" onChange={handleFileChange} />
                </label>
            </div>
            <div>
                <label>
                    Registration Type:
                    <select value={type} onChange={(e) => setType(e.target.value)}>
                        <option value="Checkin">Check In</option>
                        <option value="Checkout">Check Out</option>
                        <option value="LunchCheckin">Lunch Check In</option>
                        <option value="LunchCheckout">Lunch Check Out</option>
                    </select>
                </label>
            </div>
            <button onClick={handleUpload} disabled={!file || loading}>
                {loading ? 'Uploading...' : 'Upload'}
            </button>
        </div>
    );
};

export default RegistrationUpload;
```

## Error Handling
The service handles various error scenarios:

1. **Empty file**: Returns 400 Bad Request
2. **Invalid file type**: Only .xlsx and .xls files are accepted
3. **User not found**: Skips rows where the email doesn't match any user
4. **Invalid date/time**: Uses current timestamp as fallback
5. **Empty email**: Skips rows with empty email addresses

## Notes
- The service assumes the first row contains headers and skips it
- Users must exist in the database before uploading registrations
- The system will create Registration objects even if some rows fail to process
- All successfully processed registrations are saved to the database
- Failed rows are logged but don't stop the overall process