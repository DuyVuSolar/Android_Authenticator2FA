package com.kuemiin.reversevoice.utils.refresh

/*
* Copyright (C) 2015 Pedro Paulo de Amorim
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
interface MaterialHeadListener {
    fun onComlete(materialRefreshLayout: com.kuemiin.reversevoice.utils.refresh.MaterialRefreshLayout)
    fun onBegin(materialRefreshLayout: com.kuemiin.reversevoice.utils.refresh.MaterialRefreshLayout)
    fun onPull(materialRefreshLayout: com.kuemiin.reversevoice.utils.refresh.MaterialRefreshLayout, fraction: Float)
    fun onRelease(materialRefreshLayout: com.kuemiin.reversevoice.utils.refresh.MaterialRefreshLayout, fraction: Float)
    fun onRefreshing(materialRefreshLayout: com.kuemiin.reversevoice.utils.refresh.MaterialRefreshLayout)
}
