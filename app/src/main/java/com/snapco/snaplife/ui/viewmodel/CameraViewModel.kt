package com.snapco.snaplife.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otaliastudios.cameraview.controls.Facing

class CameraViewModel : ViewModel(){

    private val _facing = MutableLiveData(Facing.BACK)
    val facing: LiveData<Facing> = _facing

    fun switchCamera() {
        _facing.value = if (_facing.value == Facing.BACK) Facing.FRONT else Facing.BACK
    }

}