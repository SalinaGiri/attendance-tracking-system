package be.ucll.controller;

import be.ucll.dto.DtoMapper;
import be.ucll.dto.EventDTO;
import be.ucll.dto.GroupDTO;
import be.ucll.model.Event;
import be.ucll.model.Group;
import be.ucll.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8000}")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDTO> getAllEvents() {
        return eventService.getAllEvents().stream()
                .map(DtoMapper::toEventDTO)
                .toList();
    }

    @PostMapping("/add_event")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);
        return event.map(e -> ResponseEntity.ok(DtoMapper.toEventDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/groups")
    public ResponseEntity<List<GroupDTO>> getGroupsByEventId(@PathVariable(name = "id") Long eventId){
        List<Group> groups = eventService.getGroupsByEventId(eventId);
        List<GroupDTO> groupDTOS = new ArrayList<>();
        for (Group group : groups){
            groupDTOS.add(DtoMapper.toGroupDTO(group));
        }
        return ResponseEntity.ok(groupDTOS);
    }

    @PostMapping("/{id}/rotation-code")
    public ResponseEntity<String> changeEventRotationCode(@PathVariable Long id) {
        return new ResponseEntity<>('"' + eventService.changeEventRotationCode(id) + '"', HttpStatus.OK);
    }
}