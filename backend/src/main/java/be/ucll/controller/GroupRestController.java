package be.ucll.controller;

import be.ucll.dto.DtoMapper;
import be.ucll.dto.GroupDTO;
import be.ucll.model.Group;
import be.ucll.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8000}")
public class GroupRestController {
    private final GroupService groupService;

    public GroupRestController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<GroupDTO> getAllGroups(){
        return groupService.findAllGroups().stream().map(DtoMapper::toGroupDTO).toList();
    }
    
    @PostMapping
    public GroupDTO createGroup(@RequestBody Group group) {
        Group saved = groupService.saveGroup(group);
        return DtoMapper.toGroupDTO(saved);
    }
    

    @GetMapping("/{courseId}")
    public List<GroupDTO> findGroupsByCourseId(@PathVariable Long courseId) {
        return groupService.findGroupsByCourseId(courseId).stream().map(DtoMapper::toGroupDTO).toList();
    }

}
