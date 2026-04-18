package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.response.SearchStaffDto;
import hu.pogany.freshPotato.dto.response.StaffDto;
import hu.pogany.freshPotato.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@Validated
@Tag(name = "Staff", description = "Browse cast and crew members")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff details", description = "Returns one staff member with played and directed movie lists")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff found", content = @Content(schema = @Schema(implementation = StaffDto.class))),
            @ApiResponse(responseCode = "404", description = "Staff not found", content = @Content(schema = @Schema(type = "string", example = "Staff not found")))
    })
    public StaffDto getById(@Parameter(description = "Staff id", example = "31") @PathVariable int id) {
        return staffService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Get paginated staff list", description = "Returns staff members in paginated form")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff page returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchStaffDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid paging parameters", content = @Content(schema = @Schema(type = "string", example = "size must be less than or equal to 100")))
    })
    public List<SearchStaffDto> getAll(
            @Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "30") @RequestParam(defaultValue = "30") @Max(100) int size
    ) {
        return staffService.getAll(page, size);
    }
}

