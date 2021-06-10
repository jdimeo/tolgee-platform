/*
 * Copyright (c) 2020. Tolgee
 */

package io.tolgee.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.tolgee.dtos.request.GenerateSlugDto
import io.tolgee.service.OrganizationService
import io.tolgee.service.ProjectService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/v2/address-part", "/api/address-part"])
@Tag(name = "Address Part generation")
open class SlugController(
        private val organizationService: OrganizationService,
        private val projectService: ProjectService,
) {

    @GetMapping("/validate-organization/{slug}")
    @Operation(summary = "Validate organization address part")
    open fun validateOrganizationSlug(
            @PathVariable("slug") slug: String
    ): Boolean {
        return organizationService.validateSlugUniqueness(slug)
    }


    @GetMapping("/validate-project/{slug}")
    @Operation(summary = "Validate project address part")
    open fun validateProjectSlug(
            @PathVariable("slug") slug: String
    ): Boolean {
        return projectService.validateSlugUniqueness(slug)
    }

    @PostMapping("/generate-organization", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Generate organization address part")
    open fun generateOrganizationSlug(
            @RequestBody @Valid dto: GenerateSlugDto
    ): String {
        return """"${organizationService.generateSlug(dto.name!!, dto.oldSlug)}""""
    }


    @PostMapping("/generate-project", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Generate project address part")
    open fun generateProjectSlug(
            @RequestBody @Valid dto: GenerateSlugDto
    ): String {
        return """"${projectService.generateSlug(dto.name!!, dto.oldSlug)}""""
    }
}