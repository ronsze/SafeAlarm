package com.myproject.safealarm.feature.guard.alarm

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import kr.sdbk.domain.model.guard.Alarm

class AlarmFragment: BaseFragment<AlarmViewModel>() {
    override val fragmentViewModel: AlarmViewModel by viewModels()

    @Composable
    override fun Root() {
        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()
        when (uiState) {
            AlarmViewModel.AlarmUiState.Loading -> fragmentViewModel::loadData
            is AlarmViewModel.AlarmUiState.Loaded -> View()
        }
    }

    @Composable
    private fun View() {
        var isAddAlarmDialogVisible by remember { mutableStateOf(false) }
        if (isAddAlarmDialogVisible) AddAlarmDialog(
            onConfirm = this@AlarmFragment::addAlarm,
            onDismissRequest = { isAddAlarmDialogVisible = false }
        )

        Column {
            BaseToolbar(
                titleComposable = BaseToolbarDefaults.defaultTitle(
                    title = stringResource(id = R.string.alarm_set)
                ),
                rearComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    icon = Icons.Filled.Add,
                    onClick = { isAddAlarmDialogVisible = true }
                )
            )
            
            val alarmList by fragmentViewModel.alarmList.collectAsStateWithLifecycle()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                items(alarmList) {
                    AlarmItem(alarm = it)
                }
            }
        }
    }
    
    @Composable
    private fun AlarmItem(alarm: Alarm) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .border(1.5.dp, Color.LightGray, RoundedCornerShape(12.dp))
        ) {
            BaseText(
                text = "${alarm.hour}시 ${alarm.min}분"
            )
        }
    }
    
    private fun addAlarm(alarm: Alarm) {
//        var alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        var intent = Intent(context, alarmManagerReceiver::class.java)
//        intent.putExtra("num", requestNum)
//        intent.putExtra("hour", tHour)
//        intent.putExtra("min", tMin)
//        var pIntent = PendingIntent.getBroadcast(context, requestNum, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//
//        var day = Date(System.currentTimeMillis()).date
//        var hour = tHour.toInt()
//        val minute = tMin.toInt()
//        App.prefs.alarmTime = "${hour}시   ${minute}분"
//
//        val cal = getCalendar(day, hour, minute, add)
//
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pIntent)
//        App.prefs.alarmCount = 1
//        Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun removeAlarm(){
//        var alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        var intent = Intent(context, alarmManagerReceiver::class.java)
//        var pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//
//        alarmManager.cancel(pIntent)
//        App.prefs.alarmCount = 0
//        App.prefs.alarmTime = ""
//
//        Toast.makeText(context, "제거되었습니다.", Toast.LENGTH_SHORT).show()
//
//        onResume()
    }

//    inner class reAlarm: BroadcastReceiver(){
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if(intent != null){
//                val hour = intent.getStringExtra("hour")
//                val min = intent.getStringExtra("min")
//                val requestNum = intent.getIntExtra("num", 999999)
//                addAlarm(1, requestNum, hour!!, min!!)
//            }
//        }
//    }
}