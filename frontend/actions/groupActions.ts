"use server"

import GroupService from "@services/GroupService";

export async function fetchGroupsByCourseIdAction(courseId: number ) {
  if (!courseId) throw new Error("missing courseId")
  return GroupService.findGroupsByCourseId(courseId);
}