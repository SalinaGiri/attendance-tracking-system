"use server"

import CourseService from "@services/CourseService"
import { Student } from "@types"

export async function createCourseAction(name: string, description:string) {
  if (!name || ! description) throw new Error("missing name or description")
  return CourseService.addCourse(name,description)
}

export async function fetchCourseByCourseIdAction(courseId: number ) {
  if (!courseId) throw new Error("missing courseId")
  return CourseService.findCourseById(courseId);
}

export async function uploadGroupAction(courseId: number, groupName: string, file: File) {
  if (!file || !groupName || !courseId) throw new Error("Missing file, groupName or courseId");
  return CourseService.uploadStudentsToGroup(courseId, groupName, file);
}

export async function createGroupAction(courseId: number, groupName: string) {
  if (!courseId || !groupName) throw new Error("missing courseId or groupName")
  return CourseService.createGroup(courseId, groupName)
}

export async function deleteGroupAction(courseId: number, groupName: string) {
  if (!courseId || !groupName) throw new Error("missing courseId or groupName")
  return CourseService.deleteGroup(courseId, groupName)
}

export async function addStudentToGroupManuallyAction(courseId: number, groupName: string, newStudent: Student) {
  if (!courseId || !groupName || !newStudent) throw new Error("missing courseId, groupName or student");
  const groupNameDecoded = decodeURI(groupName)
  return CourseService.addStudentToGroupManually(courseId, groupNameDecoded, newStudent)
}

export async function removeStudentFromGroupAction(courseId: number, groupName: string, studentNumber: string) {
  if (!courseId || !groupName || !studentNumber) throw new Error("missing courseId, groupName or studentNumber");
  const groupNameDecoded = decodeURI(groupName)
  return CourseService.removeStudentFromGroup(courseId, groupNameDecoded, studentNumber)
}

export async function getGroupWithStudentCountAction(courseId: number, groupName: string) {
  if (!courseId || !groupName ) throw new Error("missing courseId or groupName");
  return CourseService.getGroupWithStudentCount(courseId, groupName)
}