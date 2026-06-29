export type User = {
  id?: number;
  name?: string;
  age?: number;
  email?: string;
  password?: string;
}

export type Student = {
  id?: number;
  rnumber?: string;
  studentNumber?: string;
  lastName: string;
  firstName: string;
}

export type Group = {
  id?: number;
  name?: string;
  assignedStudents?: Array<Student>;
  studentListUploaded?: boolean;
}

export type GroupWithCount = Group & {
  studentCount: number;
};

export type Event = {
  id?: number;
  eventName?: string;
  checkInTime?: Date;
  checkOutTime?: Date;
  eventType: "CheckIn" | "CheckOut" | "CheckInOut";
  requiredRegistration?: string;
  groups?: Array<Group>;
  courseId?: number;
}

export type EventInput = {
  id?: number;
  eventName?: string;
  checkInTime?: string;
  checkOutTime?: string;
  requiredRegistration?: string;
  groups?: Array<Group>;
  // courseId?: string;
  course?: Course;
}

export type Attendee = {
  id: number;
  rnumber: string;
  student: Student;
  event: Event;
  date: Date;
  type?: string;
  status: "present" | "late" | "absent" | "unexpected";
  validAbsence: boolean;
  studentNumber?: string;
  firstName?: string;
  lastName?: string;
  eventName?: string;
}

export type AttendeeV2 = {
    id: number;
    checkInId: number;
    checkOutId: number;
    rnumber: string;
    student: Student;
    event: Event;
    checkInTime: Date;
    checkOutTime: Date;
    timeDelta: number;
    timedeltaStr: string;
    type?: string;
    status: "Present" | "Late" | "Absent" | "Unexpected" | "HalfPresent" | "AbsentLate";
    validAbsence: boolean;
    studentNumber?: string;
    firstName?: string;
    lastName?: string;
    eventName?: string;
}

export type AttendeeProjection = {
  studentNumber: string;
  firstName: string;
  lastName: string;
  validAbsenceCount: number;
  absenceCount: number;
  unexpectedCount: number;
  halfPresentCount: number;
  absentLateCount: number;
  lateCount: number;
}


export type Course = {
  id?: number;
  name?: string;
  description?: string;
}

export type StatusMessage = {
  message: string;
  type: 'error' | 'success';
}

export type FilterCounts = {
    allCount: number;
    expectedCount: number;
    unexpectedPresentCount: number;
    presentCount: number;
    absentCount: number;
    lateCheckInCount: number;
    lateCheckOutCount: number;
    checkInCount: number;
    checkOutCount: number;
}

export type SelfRegistration = {
    eventId: number;
    rNumber: string;
    rotatingCode: string;
    registrationType: string;
}
