import client from './client'

export const productionAPI = {
  // Batches
  getBatches:       ()       => client.get('/production-batches'),
  getBatch:         (id)     => client.get(`/production-batches/${id}`),
  createBatch:      (data)   => client.post('/production-batches', data),
  updateBatch:      (id, d)  => client.put(`/production-batches/${id}`, d),
  cancelBatch:      (id)     => client.post(`/production-batches/${id}/cancel`),

  // Design-first production order
  startProductionOrder: (data) => client.post('/production/start-order', data),

  // Stages
  getStages:        ()       => client.get('/production-stages'),

  // Modules
  getModules:       ()       => client.get('/production-modules'),

  // Supervisors
  getSupervisors:   ()       => client.get('/supervisors'),

  // Dispatches
  getAssignments:   ()       => client.get('/module-assignments'),
  getMyAssignments: ()       => client.get('/module-assignments/mine'),
  getByBatch:       (batchId)=> client.get(`/module-assignments/batch/${batchId}`),
  getOverdue:       ()       => client.get('/module-assignments/overdue'),
  dispatch:         (data)   => client.post('/module-assignments', data),
  returnAssignment: (id, d)  => client.post(`/module-assignments/${id}/return`, d),

  // Flow tracking
  getBatchFlow:     (batchId)=> client.get(`/production-flow/${batchId}`),

  // Catalog
  getSuits:         ()       => client.get('/suits'),
  createSuit:       (data)   => client.post('/suits', data),
  getDesigns:       ()       => client.get('/designs'),
  getDesign:        (id)     => client.get(`/designs/${id}`),
  createDesign:     (data)   => client.post('/designs', data),
  getCategories:    ()       => client.get('/categories'),
  createCategory:   (data)   => client.post('/categories', data),
  getSizes:         ()       => client.get('/sizes'),
  getDesignTypes:   ()       => client.get('/design-types'),
  createDesignType: (data)   => client.post('/design-types', data),
  getRequiredStages: (designId) => client.get(`/designs/${designId}/required-stages`),
  saveRequiredStages: (designId, stageIds) =>
    client.put(`/designs/${designId}/required-stages`, stageIds),
  getDesigningWorkTypes: () => client.get('/designing-work-types'),
  getFillingWorkTypes:   () => client.get('/filling-work-types'),
  createSize: (data) => client.post('/sizes', data),

  // Design images
  uploadDesignImage: (designId, file) => {
    const form = new FormData()
    form.append('file', file)
    // Do not set Content-Type — browser must add multipart boundary
    return client.post(`/design-images/upload/${designId}`, form)
  },
  getDesignImages: (designId) => client.get(`/design-images/design/${designId}`),
  deleteDesignImage: (imageId) => client.delete(`/design-images/${imageId}`),
  updateDesign: (id, data) => client.put(`/designs/${id}`, data),
  deleteDesign: (id) => client.delete(`/designs/${id}`),
}
